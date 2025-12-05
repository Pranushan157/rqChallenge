package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeSingleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService {

    private final RestTemplate restTemplate;
    private static final String MOCK_API_URL = "http://localhost:8112/api/v1/employee";

    @Autowired
    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Employee> getAllEmployees() {
        try{
            log.info("Fetching all employees from mock API");
            EmployeeListResponse response = restTemplate.getForObject(MOCK_API_URL, EmployeeListResponse.class);
            List<Employee> employees = response.getData();
            log.info("Successfully fetched {} employees", employees.size());
            return employees;
        } catch (Exception e) {
            log.error("Error fetching employees from mock API", e);
            throw new RuntimeException("Failed to retrieve employees", e);
        }
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        try {
            log.info("Searching employees by name: {}", searchString);
            List<Employee> allEmployees = getAllEmployees();
            List<Employee> filtered = allEmployees.stream()
                    .filter(emp -> emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());
            log.info("Found {} employees matching search: {}", filtered.size(), searchString);
            return filtered;
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            throw new RuntimeException("Failed to search employees", e);
        }
    }

    public Employee getEmployeeById(String id) {
        try {
            log.info("Fetching employee by id: {}", id);
            String url = MOCK_API_URL + "/" + id;
            EmployeeSingleResponse response = restTemplate.getForObject(url, EmployeeSingleResponse.class);
            Employee employee = response.getData();
            log.info("Successfully retrieved employee with id: {}", id);
            return employee;
        } catch (Exception e) {
            log.error("Error fetching employee by id: {}", id, e);
            throw new RuntimeException("Failed to retrieve employee with id: " + id, e);
        }
    }

    public Integer getHighestSalaryOfEmployees() {
        try {
            log.info("Fetching highest salary");
            List<Employee> allEmployees = getAllEmployees();
            Integer highestSalary = allEmployees.stream()
                    .map(Employee::getEmployeeSalary)
                    .max(Comparator.naturalOrder())
                    .orElse(0);
            log.info("Highest salary: {}", highestSalary);
            return highestSalary;
        } catch (Exception e) {
            log.error("Error fetching highest salary", e);
            throw new RuntimeException("Failed to retrieve highest salary", e);
        }
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        try {
            log.info("Fetching top 10 highest earning employees");
            List<Employee> allEmployees = getAllEmployees();
            List<String> topTenNames = allEmployees.stream()
                    .sorted(Comparator.comparingInt(Employee::getEmployeeSalary).reversed())
                    .limit(10)
                    .map(Employee::getEmployeeName)
                    .collect(Collectors.toList());
            log.info("Retrieved top 10 highest earning employees");
            return topTenNames;
        } catch (Exception e) {
            log.error("Error fetching top 10 highest earning employees", e);
            throw new RuntimeException("Failed to retrieve top 10 highest earning employees", e);
        }
    }

    public Employee createEmployee(EmployeeInput employeeInput) {
        try {
            log.info("Creating employee with name: {}", employeeInput.getName());
            EmployeeSingleResponse response = restTemplate.postForObject(MOCK_API_URL, employeeInput, EmployeeSingleResponse.class);
            Employee employee = response.getData();
            log.info("Successfully created employee with id: {}", employee.getId());
            return employee;
        } catch (Exception e) {
            log.error("Error creating employee", e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    public String deleteEmployeeById(String id) {
        try {
            log.info("Deleting employee with id: {}", id);
            Employee employee = getEmployeeById(id);
            String employeeName = employee.getEmployeeName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("name", employeeName);

            HttpEntity<Map<String,String>> request = new HttpEntity<>(body, headers);

            restTemplate.exchange(MOCK_API_URL, HttpMethod.DELETE, request, String.class);
            log.info("Successfully deleted employee with id: {}", id);
            return employeeName;
        } catch (Exception e) {
            log.error("Error deleting employee with id: {}", id, e);
            throw new RuntimeException("Failed to delete employee with id: " + id, e);
        }
    }
}
