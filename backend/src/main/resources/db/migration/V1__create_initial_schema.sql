CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name),
    PRIMARY KEY (id)
);

CREATE TABLE appointment_statuses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL,
    CONSTRAINT uk_appointment_statuses_name UNIQUE (name),
    PRIMARY KEY (id)
);

CREATE TABLE services (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    is_active BIT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_services_name UNIQUE (name),
    PRIMARY KEY (id)
);

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(190) NOT NULL,
    phone VARCHAR(25) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    PRIMARY KEY (id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE barbers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    specialty VARCHAR(150) NOT NULL,
    is_active BIT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_barbers_user UNIQUE (user_id),
    PRIMARY KEY (id),
    CONSTRAINT fk_barbers_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE appointments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    barber_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME(6) NOT NULL,
    end_time TIME(6) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(1000),
    payment_method VARCHAR(30),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_appointments_client FOREIGN KEY (client_id) REFERENCES users (id),
    CONSTRAINT fk_appointments_barber FOREIGN KEY (barber_id) REFERENCES barbers (id),
    CONSTRAINT fk_appointments_status FOREIGN KEY (status_id) REFERENCES appointment_statuses (id)
);

CREATE TABLE appointment_details (
    id BIGINT NOT NULL AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uk_appointment_details_service UNIQUE (appointment_id, service_id),
    PRIMARY KEY (id),
    CONSTRAINT fk_appointment_details_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_appointment_details_service FOREIGN KEY (service_id) REFERENCES services (id)
);

INSERT INTO roles (name) VALUES ('ADMIN'), ('BARBER'), ('CLIENT');
INSERT INTO appointment_statuses (name) VALUES ('PENDIENTE'), ('CONFIRMADA'), ('COMPLETADA'), ('CANCELADA');
