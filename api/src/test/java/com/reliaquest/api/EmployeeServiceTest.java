package com.reliaquest.api;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeSingleResponse;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;

class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeSingleResponse singleResponse;
    private EmployeeListResponse listResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        employee = new Employee();
        employee.setId("123");
        employee.setEmployeeName("John Doe");
        employee.setEmployeeSalary(5000);

        singleResponse = new EmployeeSingleResponse();
        singleResponse.setData(employee);

        listResponse = new EmployeeListResponse();
        listResponse.setData(Collections.singletonList(employee));
    }

    @Test
    void testGetAllEmployees_success() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeListResponse.class)))
                .thenReturn(listResponse);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void testGetAllEmployees_failure() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeListResponse.class)))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void testGetEmployeeById_success() {
        when(restTemplate.getForObject(contains("/123"), eq(EmployeeSingleResponse.class)))
                .thenReturn(singleResponse);

        Employee result = employeeService.getEmployeeById("123");

        assertEquals("John Doe", result.getEmployeeName());
    }

    @Test
    void testGetEmployeesByNameSearch_success() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeListResponse.class)))
                .thenReturn(listResponse);

        List<Employee> result = employeeService.getEmployeesByNameSearch("john");

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void testGetHighestSalary_success() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeListResponse.class)))
                .thenReturn(listResponse);

        Integer highest = employeeService.getHighestSalaryOfEmployees();

        assertEquals(5000, highest);
    }

    @Test
    void testGetTopTenEmployees_success() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeListResponse.class)))
                .thenReturn(listResponse);

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0));
    }

    @Test
    void testCreateEmployee_success() {
        EmployeeInput input = new EmployeeInput();
        input.setName("John Doe");
        input.setAge(30);
        input.setSalary(5000);

        when(restTemplate.postForObject(anyString(), eq(input), eq(EmployeeSingleResponse.class)))
                .thenReturn(singleResponse);

        Employee result = employeeService.createEmployee(input);

        assertEquals("123", result.getId());
    }

    @Test
    void testDeleteEmployee_success() {
        // Mock getEmployeeById
        when(restTemplate.getForObject(contains("/123"), eq(EmployeeSingleResponse.class)))
                .thenReturn(singleResponse);

        // Mock DELETE request
        when(restTemplate.exchange(
                eq("http://localhost:8112/api/v1/employee"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("true"));

        String result = employeeService.deleteEmployeeById("123");

        assertEquals("John Doe", result);
    }

    @Test
    void testDeleteEmployee_failure() {
        when(restTemplate.getForObject(anyString(), eq(EmployeeSingleResponse.class)))
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployeeById("123"));
    }
}
