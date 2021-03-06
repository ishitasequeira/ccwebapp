package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.NutritionalInformationRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.timgroup.statsd.StatsDClient;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.Logger;

@Service

public class RecipeService {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(RecipeService.class.getName());
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NutritionalInformationRepository nutritionalInformationRepository;
    @Autowired
    private RecipeHelper recipeHelper;
    @Autowired
    StatsDClient statsDClient;

    @Autowired
    ImageService imageService;
    @Autowired
    private AmazonSNS amazonSNS;
    @Value("${webapp.domain}")
    private String webappDomain;
    @Value("${sns.topic.arn}")
    private String snsTopicArn;

    public ResponseEntity<Recipe> getRecipe(UUID id){
        Optional<Recipe> dbRecord = recipeRepository.findRecipesById(id);
        if (dbRecord.isPresent()) {
            LOGGER.info("Recipe with recipeId "+ id + " found...");
            long start = System.currentTimeMillis();
            Recipe dbRecipe = dbRecord.get();
            statsDClient.time("dbquery.get.recipe", (System.currentTimeMillis() - start));
            return new ResponseEntity<>(dbRecipe, HttpStatus.OK);
        } else {
            LOGGER.error("RecipeId "+id+ " is invalid");
            throw new RecipeNotFoundException("Recipe Id is invalid");
        }
    }

    public ResponseEntity<Recipe> deleteRecipe(UUID id, Authentication authentication) throws Exception {

        Optional<Recipe> dbRecordRecipe = recipeRepository.findById(id);
        if (!dbRecordRecipe.isPresent()) {
            LOGGER.error("Recipe is not present!!");
            throw new RecipeNotFoundException("Recipe is not present!!");
        }
        Recipe recipeDb = dbRecordRecipe.get();
        Optional<User> dbUser = userRepository.findById(recipeDb.getAuthorId());
        if (!dbUser.isPresent()) {
            LOGGER.error("Invalid userId");
            throw new InvalidInputException("Invalid user id");
            }
        if (!dbUser.get().getEmailaddress().equals(authentication.getName())) {
            LOGGER.error("No authorization for user with userEmailID:"+authentication.getName());
            throw new UserNotAuthorizedException("You are not authorized to make changes!!");
                }
        LOGGER.info("Recipe with recipeID:"+id+" has been deleted!!!");
        if (recipeDb.getImage() != null) {
            imageService.deleteImage(recipeDb.getImage().getId(),recipeDb);
        }
        long start = System.currentTimeMillis();
        recipeRepository.delete(recipeDb);
        statsDClient.time("dbquery.delete.recipe", (System.currentTimeMillis() - start));
        return new ResponseEntity<Recipe>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Recipe> saveRecipe(Recipe recipe, Authentication authentication) {
        //get user's id
        Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecord.isPresent()) {

            recipeHelper.isRecipeValid(recipe);

            User user = dbRecord.get();
            recipe.setAuthorId(user.getId());
            recipe.setTotal_time_in_min(recipe.getCook_time_in_min() + recipe.getPrep_time_in_min());
            long start = System.currentTimeMillis();
            recipeRepository.save(recipe);
            statsDClient.time("dbquery.save.recipe", (System.currentTimeMillis() - start));
            LOGGER.info("Recipe has been created...");
            return new ResponseEntity<Recipe>(recipe, HttpStatus.CREATED);
        }
        LOGGER.error("The recipe creation has been failed...");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Recipe> updateRecipe(@Valid Recipe recipe, Authentication authentication, String recipeId) throws Exception {
        //get user's id
        Optional<Recipe> dbRecipe = recipeRepository.findById(UUID.fromString(recipeId));
        if (!dbRecipe.isPresent()){
            LOGGER.error("Recipe with recipeId "+recipe.getId()+" is not present");
            throw new RecipeNotFoundException("Recipe is not present!!");
            }
        Optional<User> dbUser = userRepository.findById(dbRecipe.get().getAuthorId());
        if (!dbUser.isPresent()) {
            LOGGER.error("Error has been occured for recipeId "+recipe.getId());
            throw new InvalidInputException("Unknown error");
        }
        if (!dbUser.get().getEmailaddress().equals(authentication.getName())) {
            LOGGER.error("User are not authorized to make changes!!");
            throw new UserNotAuthorizedException("You are not authorized to make changes!!");
            }
        recipeHelper.isRecipeValid(recipe);
        if (dbRecipe.get() != null) {
            dbRecipe.get().setCook_time_in_min(recipe.getCook_time_in_min());
            dbRecipe.get().setPrep_time_in_min(recipe.getPrep_time_in_min());
            dbRecipe.get().setTotal_time_in_min(dbRecipe.get().getCook_time_in_min() + dbRecipe.get().getPrep_time_in_min());
            dbRecipe.get().setIngredients(recipe.getIngredients());
            dbRecipe.get().setSteps(recipe.getSteps());
            dbRecipe.get().setTitle(recipe.getTitle());
            dbRecipe.get().setCuisine(recipe.getCuisine());
            dbRecipe.get().setServings(recipe.getServings());
            dbRecipe.get().setNutrition_information(recipe.getNutrition_information());
        }
        Recipe rc = dbRecipe.get();
        long start = System.currentTimeMillis();
        recipeRepository.save(rc);
        statsDClient.time("dbquery.update.recipe", (System.currentTimeMillis() - start));
        LOGGER.info("Recipe with recipeID " +recipe.getId() + " has been updated....");
        return new ResponseEntity<Recipe>(rc, HttpStatus.OK);
    }

    public ResponseEntity<Recipe> getLatestRecipe(){
        List<Recipe> dbRecord = recipeRepository.findTop1ByOrderByCreatedtsDesc();
        if(dbRecord.size()==1) {
            LOGGER.info("Finding latest recipe ...");
            long start = System.currentTimeMillis();
            Recipe dbRecipe = dbRecord.get(0);
            statsDClient.time("dbquery.get.latestRecipe", (System.currentTimeMillis() - start));
            return new ResponseEntity<Recipe>(dbRecipe, HttpStatus.OK);
        }
        LOGGER.info("No Recipes available!!");
        throw new RecipeNotFoundException("No Recipes available!!");
    }

    public ResponseEntity getAllRecipesAndEmailUser(String authorEmail) {
        LOGGER.info("Getting all recipes created by user : " + authorEmail);
        List<Recipe> userRecipes = new ArrayList<>();
        Optional<User> dbUser = userRepository.findUserByEmailaddress(authorEmail);
        if(dbUser.isPresent()) {
            LOGGER.info("User found, fetching recipes");
            userRecipes = recipeRepository.findAllByAuthorId(dbUser.get().getId());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("from", "noreply@"+webappDomain);
        jsonObject.put("to", authorEmail);

        JSONArray jsonArray = new JSONArray();
        for (Recipe recipe : userRecipes) {
            LOGGER.info("Found recipe: " + recipe.getId());
            jsonArray.put(constructRecipeURL(recipe.getId().toString()));
        }

        jsonObject.put("recipes", jsonArray);
        LOGGER.info("JSON string created: " + jsonObject.toString());
        LOGGER.info("Publishing the message to SNS...");

        PublishResult publishResult = amazonSNS.publish(new PublishRequest(snsTopicArn, jsonObject.toString()));

        LOGGER.info("SNS message published: " + publishResult.toString());

        return new ResponseEntity(HttpStatus.OK);
    }

    private String constructRecipeURL(String recipeId) {
        return ("https://" + webappDomain + "/v1/recipe/" + recipeId);
    }

}
