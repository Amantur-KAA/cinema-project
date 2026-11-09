package kg.alatoo.cinema.service;

import kg.alatoo.cinema.dao.CustomerDao;
import kg.alatoo.cinema.exception.NotFoundException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Customer;

import java.util.List;

/** Правила для клиентов. Проверка полей — в классе Customer, уникальность телефона — в базе. */
public class CustomerService {
    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public Customer addCustomer(String fullName, String phone, String email, boolean student) {
        return customerDao.create(new Customer(fullName, phone, email, student));
    }

    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    public Customer getCustomer(int id) {
        Customer found = customerDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Клиент #" + id + " не найден");
        }
        return found;
    }

    public List<Customer> searchCustomers(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Введите текст для поиска");
        }
        return customerDao.search(text.trim());
    }

    public Customer updateCustomer(int id, String fullName, String phone, String email, boolean student) {
        Customer customer = getCustomer(id);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setStudent(student);
        customerDao.update(customer);
        return customer;
    }

    public void deleteCustomer(int id) {
        if (!customerDao.delete(id)) {
            throw new NotFoundException("Клиент #" + id + " не найден");
        }
    }
}
