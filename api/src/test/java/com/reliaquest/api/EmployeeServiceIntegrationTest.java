package com.reliaquest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeSingleResponse;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class EmployeeServiceIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    private EmployeeService employeeService;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;
    private static final String MOCK_API_URL = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        Employee emp1 = createEmployee("1", "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        Employee emp2 = createEmployee("2", "Jane Smith", 60000, 28, "Product Manager", "jane@test.com");
        List<Employee> employees = Arrays.asList(emp1, emp2);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<Employee> result = employeeService.getAllEmployees();

        mockServer.verify();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
        assertEquals("Jane Smith", result.get(1).getEmployeeName());
    }

    @Test
    void getAllEmployees_ApiFailure_ThrowsException() {
        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.getAllEmployees());
        assertTrue(exception.getMessage().contains("Failed to retrieve employees"));
    }

    @Test
    void getEmployeesByNameSearch_FindsMatchingEmployees() throws Exception {
        Employee emp1 = createEmployee("1", "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        Employee emp2 = createEmployee("2", "Jane Smith", 60000, 28, "Product Manager", "jane@test.com");
        Employee emp3 = createEmployee("3", "John Smith", 55000, 32, "Developer", "johnsmith@test.com");
        List<Employee> employees = Arrays.asList(emp1, emp2, emp3);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<Employee> result = employeeService.getEmployeesByNameSearch("John");

        mockServer.verify();
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getEmployeeName().contains("John")));
    }

    @Test
    void getEmployeesByNameSearch_CaseInsensitive() throws Exception {
        // Arrange
        Employee emp1 = createEmployee("1", "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        List<Employee> employees = Arrays.asList(emp1);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<Employee> result = employeeService.getEmployeesByNameSearch("JOHN");

        mockServer.verify();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeesByNameSearch_NoMatches_ReturnsEmptyList() throws Exception {
        Employee emp1 = createEmployee("1", "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        List<Employee> employees = Arrays.asList(emp1);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<Employee> result = employeeService.getEmployeesByNameSearch("NonExistent");

        mockServer.verify();
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        String employeeId = "1";
        Employee employee = createEmployee(employeeId, "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        EmployeeSingleResponse response = new EmployeeSingleResponse();
        response.setData(employee);

        mockServer.expect(requestTo(MOCK_API_URL + "/" + employeeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        Employee result = employeeService.getEmployeeById(employeeId);

        mockServer.verify();
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getEmployeeName());
    }

    @Test
    void getEmployeeById_NotFound_ThrowsException() {
        String employeeId = "999";
        mockServer.expect(requestTo(MOCK_API_URL + "/" + employeeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.getEmployeeById(employeeId));
        assertTrue(exception.getMessage().contains("Failed to retrieve employee with id: " + employeeId));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        Employee emp1 = createEmployee("1", "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        Employee emp2 = createEmployee("2", "Jane Smith", 75000, 28, "Product Manager", "jane@test.com");
        Employee emp3 = createEmployee("3", "Bob Johnson", 60000, 32, "Developer", "bob@test.com");
        List<Employee> employees = Arrays.asList(emp1, emp2, emp3);
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        Integer result = employeeService.getHighestSalaryOfEmployees();

        mockServer.verify();
        assertEquals(75000, result);
    }

    @Test
    void getHighestSalaryOfEmployees_EmptyList_ReturnsZero() throws Exception {
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(Arrays.asList());

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        Integer result = employeeService.getHighestSalaryOfEmployees();

        mockServer.verify();
        assertEquals(0, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Employee 1", 100000, 30, "CEO", "e1@test.com"),
                createEmployee("2", "Employee 2", 95000, 30, "CTO", "e2@test.com"),
                createEmployee("3", "Employee 3", 90000, 30, "CFO", "e3@test.com"),
                createEmployee("4", "Employee 4", 85000, 30, "VP", "e4@test.com"),
                createEmployee("5", "Employee 5", 80000, 30, "Director", "e5@test.com"),
                createEmployee("6", "Employee 6", 75000, 30, "Manager", "e6@test.com"),
                createEmployee("7", "Employee 7", 70000, 30, "Lead", "e7@test.com"),
                createEmployee("8", "Employee 8", 65000, 30, "Senior", "e8@test.com"),
                createEmployee("9", "Employee 9", 60000, 30, "Engineer", "e9@test.com"),
                createEmployee("10", "Employee 10", 55000, 30, "Engineer", "e10@test.com"),
                createEmployee("11", "Employee 11", 50000, 30, "Junior", "e11@test.com")
        );
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        mockServer.verify();
        assertEquals(10, result.size());
        assertEquals("Employee 1", result.get(0));
        assertEquals("Employee 10", result.get(9));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_LessThanTenEmployees() throws Exception {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Employee 1", 50000, 30, "Engineer", "e1@test.com"),
                createEmployee("2", "Employee 2", 60000, 30, "Engineer", "e2@test.com"),
                createEmployee("3", "Employee 3", 55000, 30, "Engineer", "e3@test.com")
        );
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        mockServer.verify();
        assertEquals(3, result.size());
        assertEquals("Employee 2", result.get(0));
        assertEquals("Employee 3", result.get(1));
        assertEquals("Employee 1", result.get(2));
    }

    @Test
    void createEmployee_Success() throws Exception {
        EmployeeInput input = new EmployeeInput();
        input.setName("New Employee");
        input.setSalary(70000);
        input.setAge(29);
        input.setTitle("Software Developer");

        Employee createdEmployee = createEmployee("123", "New Employee", 70000, 29, "Software Developer", "new@test.com");
        EmployeeSingleResponse response = new EmployeeSingleResponse();
        response.setData(createdEmployee);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        Employee result = employeeService.createEmployee(input);

        mockServer.verify();
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("New Employee", result.getEmployeeName());
        assertEquals(70000, result.getEmployeeSalary());
    }

    @Test
    void createEmployee_ApiFailure_ThrowsException() {
        EmployeeInput input = new EmployeeInput();
        input.setName("New Employee");
        input.setSalary(70000);

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.createEmployee(input));
        assertTrue(exception.getMessage().contains("Failed to create employee"));
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        String employeeId = "1";
        Employee employee = createEmployee(employeeId, "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        EmployeeSingleResponse getResponse = new EmployeeSingleResponse();
        getResponse.setData(employee);

        mockServer.expect(requestTo(MOCK_API_URL + "/" + employeeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(getResponse), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(org.springframework.http.HttpMethod.DELETE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andRespond(withSuccess());

        String result = employeeService.deleteEmployeeById(employeeId);

        mockServer.verify();
        assertEquals("John Doe", result);
    }

    @Test
    void deleteEmployeeById_EmployeeNotFound_ThrowsException() {
        String employeeId = "999";
        mockServer.expect(requestTo(MOCK_API_URL + "/" + employeeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.deleteEmployeeById(employeeId));
        assertTrue(exception.getMessage().contains("Failed to delete employee with id: " + employeeId));
    }

    @Test
    void deleteEmployeeById_DeleteApiFailure_ThrowsException() throws Exception {
        String employeeId = "1";
        Employee employee = createEmployee(employeeId, "John Doe", 50000, 30, "Software Engineer", "john@test.com");
        EmployeeSingleResponse getResponse = new EmployeeSingleResponse();
        getResponse.setData(employee);

        mockServer.expect(requestTo(MOCK_API_URL + "/" + employeeId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(getResponse), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(MOCK_API_URL))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withServerError());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.deleteEmployeeById(employeeId));
        assertTrue(exception.getMessage().contains("Failed to delete employee with id: " + employeeId));
    }

    private Employee createEmployee(String id, String name, Integer salary, Integer age, String title, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmployeeName(name);
        employee.setEmployeeSalary(salary);
        employee.setEmployeeAge(age);
        employee.setEmployeeTitle(title);
        employee.setEmployeeEmail(email);
        return employee;
    }
}