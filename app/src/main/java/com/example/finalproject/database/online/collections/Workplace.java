package com.example.finalproject.database.online.collections;

import com.google.gson.Gson;

public class Workplace {
    // The branch's ID:
    private String branchId;

    // Whether the branch is active or not (if not, no new shifts/employees can be added to it):
    private boolean isActive;

    // Whether or not the user is a manager at the branch:
    private boolean isManager;

    // The company name:
    private String companyName;

    // The country, city and address of the branch:
    private String country, city, address;

    // Constants that show the attribute names in the database, instead of hardcoding them:
    public static final String BRANCH_ID = Branch.BRANCH_ID;
    public static final String IS_ACTIVE = Branch.IS_ACTIVE;
    public static final String IS_MANAGER = Employee.IS_MANAGER;
    public static final String COMPANY_NAME = Branch.COMPANY_NAME;
    public static final String ADDRESS = Branch.ADDRESS;
    public static final String CITY = Branch.CITY;
    public static final String COUNTRY = Branch.COUNTRY;

    // Empty constructor, firestore requirement:
    public Workplace() {

    }

    // Getters and setters:
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * A utility method that makes it easier to create a workplace object from an existing branch.
     * @param branch A branch whom the new workplace object represent.
     * @param isManager Whether or not the user that owns the workplace object is a manager in the
     *                  branch or not.
     * @return A new workplace object consisting ot the given branch and the given isManager param.
     */
    public static Workplace fromBranch(Branch branch, boolean isManager) {
        // Create the workplace object:
        final Workplace workplace = new Workplace();
        workplace.setBranchId(branch.getBranchId());
        workplace.setManager(isManager);
        workplace.setCompanyName(branch.getCompanyName());
        workplace.setCountry(branch.getCountry());
        workplace.setCity(branch.getCity());
        workplace.setAddress(branch.getAddress());

        // Return the workplace object:
        return workplace;
    }

    public String jsonifyWorkplace() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }
}
