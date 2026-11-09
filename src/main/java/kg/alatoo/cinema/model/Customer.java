package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.util.Objects;

/** Клиент кинотеатра (таблица customers). */
public class Customer {
    private int id;
    private String fullName;
    private String phone;
    private String email;
    private boolean student;

    public Customer(int id, String fullName, String phone, String email, boolean student) {
        this.id = id;
        setFullName(fullName);
        setPhone(phone);
        setEmail(email);
        this.student = student;
    }

    public Customer(String fullName, String phone, String email, boolean student) {
        this(0, fullName, phone, email, student);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Имя клиента не может быть пустым");
        }
        if (fullName.trim().length() < 2 || fullName.trim().length() > 100) {
            throw new ValidationException("Имя клиента должно быть от 2 до 100 символов");
        }
        this.fullName = fullName.trim();
    }

    public String getPhone() {
        return phone;
    }

    /** Телефон: необязательный + в начале и от 9 до 15 цифр, например +996700123456. */
    public void setPhone(String phone) {
        if (phone == null || !phone.trim().matches("\\+?\\d{9,15}")) {
            throw new ValidationException("Телефон должен содержать 9-15 цифр, например +996700123456");
        }
        this.phone = phone.trim();
    }

    public String getEmail() {
        return email;
    }

    /** Email необязателен: пустая строка превращается в null. */
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            this.email = null;
            return;
        }
        String value = email.trim();
        if (!value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ValidationException("Неверный email: " + value);
        }
        this.email = value;
    }

    public boolean isStudent() {
        return student;
    }

    public void setStudent(boolean student) {
        this.student = student;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer other = (Customer) obj;
        return id == other.id && Objects.equals(phone, other.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, phone);
    }

    @Override
    public String toString() {
        return "#" + id + " " + fullName + ", тел. " + phone
                + (email != null ? ", " + email : "")
                + (student ? ", студент" : "");
    }
}
