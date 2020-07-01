package ca.louisechan.livefitandroid;

public class Meal {
    private String name;
    private String description;
    private String calories;
    private String imageName;
    private String warning;

    public Meal(String name, String description, String calories, String imageName, String warning) {
        this.name = name;
        this.description = description;
        this.calories = calories;
        this.imageName = imageName;
        this.warning = warning;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
