package com.example.finalproject.util;

import com.example.finalproject.database.online.collections.Employee;

public interface EmployeeActions {
    /**
     * Function responsible for promoting the given employee object from regular employee to
     * manager.
     * @param employee An employee that will be promoted ☜(ﾟヮﾟ☜)
     */
    void promote(Employee employee);

    /**
     * Function responsible for demoting the given employee object (┬┬﹏┬┬).
     * @param employee An employee that will be demoted
     */
    void demote(Employee employee);

    /**
     * Function responsible for firing the given employee object.
     * @param employee An employee that will be fired ಥ_ಥ
     */
    void fire(Employee employee);
}
