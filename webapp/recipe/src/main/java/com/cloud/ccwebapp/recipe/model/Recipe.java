package com.cloud.ccwebapp.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ReadOnlyProperty
    private UUID id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_ts")
    @ReadOnlyProperty
    @JsonProperty("created_ts")
    private Date createdts;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_ts")
    @ReadOnlyProperty
    private Date updated_ts;

    @ReadOnlyProperty
    private UUID authorId;

    @Min(value = 1, message = "cook_time_in_min must be greater than 1")
    private int cook_time_in_min;

    @Min(value = 1, message = "prep_time_in_min must be greater than 1")
    private int prep_time_in_min;

    @ReadOnlyProperty
    private int total_time_in_min;

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "cuisine cannot be blank")
    private String cuisine;

    @Range(min = 1, max = 5)
    private int servings;

    @UniqueElements
    @NotEmpty(message = "recipe must have ingredients")
    @ElementCollection
    private List<String> ingredients;

    @OneToMany(cascade = CascadeType.ALL)
    @NotEmpty(message = "recipe must have steps")
    private List<OrderedList> steps;

    @OneToOne(cascade = CascadeType.ALL)
    @NotNull(message = "recipe must have nutrition_information")
    private NutritionalInformation nutrition_information;

    @ReadOnlyProperty
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    public Recipe() {
        steps = new ArrayList<>();
        ingredients = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreatedts() {
        return createdts;
    }

    public void setCreatedts(Date createdts) {
        this.createdts = createdts;
    }

    public Date getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(Date updated_ts) {
        this.updated_ts = updated_ts;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public int getCook_time_in_min() {
        return cook_time_in_min;
    }

    public void setCook_time_in_min(int cook_time_in_min) {
        this.cook_time_in_min = cook_time_in_min;
    }

    public int getPrep_time_in_min() {
        return prep_time_in_min;
    }

    public void setPrep_time_in_min(int prep_time_in_min) {
        this.prep_time_in_min = prep_time_in_min;
    }

    public int getTotal_time_in_min() {
        return total_time_in_min;
    }

    public void setTotal_time_in_min(int total_time_in_min) {
        this.total_time_in_min = total_time_in_min;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }


    public List<OrderedList> getSteps() {
        return steps;
    }

    public void setSteps(List<OrderedList> steps) {
        this.steps = steps;
    }

    public NutritionalInformation getNutrition_information() {
        return nutrition_information;
    }

    public void setNutrition_information(NutritionalInformation nutrition_information) {
        this.nutrition_information = nutrition_information;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "image="+image+
                ", id=" + id +
                ", created_ts=" + createdts +
                ", updated_ts=" + updated_ts +
                ", author_id=" + authorId +
                ", cook_time_in_min=" + cook_time_in_min +
                ", prep_time_in_min=" + prep_time_in_min +
                ", total_time_in_min=" + total_time_in_min +
                ", title='" + title + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", servings=" + servings +
                ", ingredients=" + ingredients +
                ", steps=" + steps +
                ", nutrition_information=" + nutrition_information +
                '}';
    }

}
