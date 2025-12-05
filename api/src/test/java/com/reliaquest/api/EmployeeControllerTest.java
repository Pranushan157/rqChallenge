package com.reliaquest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllEmployees() throws Exception {
        Employee emp = new Employee();
        emp.setId("123");
        emp.setEmployeeName("John Doe");

        when(employeeService.getAllEmployees())
                .thenReturn(Collections.singletonList(emp));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"));
    }

    @Test
    void testSearchEmployees() throws Exception {
        Employee emp = new Employee();
        emp.setEmployeeName("John Doe");

        when(employeeService.getEmployeesByNameSearch("john"))
                .thenReturn(Collections.singletonList(emp));

        mockMvc.perform(get("/api/v1/employee/search/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        Employee emp = new Employee();
        emp.setId("123");
        emp.setEmployeeName("John Doe");

        when(employeeService.getEmployeeById("123"))
                .thenReturn(emp);

        mockMvc.perform(get("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("John Doe"));
    }

    @Test
    void testHighestSalary() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees())
                .thenReturn(5000);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));
    }

    @Test
    void testTopTenEmployees() throws Exception {
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenReturn(Arrays.asList("John", "Jane"));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("John"));
    }

    @Test
    void testCreateEmployee() throws Exception {
        EmployeeInput input = new EmployeeInput();
        input.setName("John");
        input.setAge(25);
        input.setSalary(5000);

        Employee created = new Employee();
        created.setId("123");
        created.setEmployeeName("John");

        when(employeeService.createEmployee(any(EmployeeInput.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        when(employeeService.deleteEmployeeById("123"))
                .thenReturn("John Doe");

        mockMvc.perform(delete("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("John Doe"));
    }
}
