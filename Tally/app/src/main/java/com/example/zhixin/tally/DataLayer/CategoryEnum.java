package com.example.zhixin.tally.DataLayer;

/**
 * Created by ZhiXin on 2019/2/18.
 */

public enum CategoryEnum {
    Food(CategoryArray.getArray()[0]),
    Cloth(CategoryArray.getArray()[1]),
    House(CategoryArray.getArray()[2]),
    Transport(CategoryArray.getArray()[3]),
    Child(CategoryArray.getArray()[4]),
    Health(CategoryArray.getArray()[5]),
    Education(CategoryArray.getArray()[6]),
    Phone(CategoryArray.getArray()[7]),
    Social(CategoryArray.getArray()[8]),
    Other(CategoryArray.getArray()[9]),
    Income(CategoryArray.getArray()[10]);


    private CategoryEnum(String val) {
        this.value = val;
    }

    private String value;

    public String value() {
        return value;
    }

    public static CategoryEnum getEnumByIndex(int index) {
        CategoryEnum categoryEnum = null;
        String val = CategoryArray.getArray()[index];
        if (val.equals(CategoryArray.getArray()[0])) {
            categoryEnum = CategoryEnum.Food;
        } else if (val.equals(CategoryArray.getArray()[1])) {
            categoryEnum = CategoryEnum.Cloth;
        } else if (val.equals(CategoryArray.getArray()[2])) {
            categoryEnum = CategoryEnum.House;
        } else if (val.equals(CategoryArray.getArray()[3])) {
            categoryEnum = CategoryEnum.Transport;
        } else if (val.equals(CategoryArray.getArray()[4])) {
            categoryEnum = CategoryEnum.Child;
        } else if (val.equals(CategoryArray.getArray()[5])) {
            categoryEnum = CategoryEnum.Health;
        } else if (val.equals(CategoryArray.getArray()[6])) {
            categoryEnum = CategoryEnum.Education;
        } else if (val.equals(CategoryArray.getArray()[7])) {
            categoryEnum = CategoryEnum.Phone;
        } else if (val.equals(CategoryArray.getArray()[8])) {
            categoryEnum = CategoryEnum.Social;
        } else if (val.equals(CategoryArray.getArray()[9])) {
            categoryEnum = CategoryEnum.Other;
        } else if (val.equals(CategoryArray.getArray()[10])) {
            categoryEnum = CategoryEnum.Income;
        }
        return categoryEnum;
    }


}
