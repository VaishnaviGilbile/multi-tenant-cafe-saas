
-- This file runs once and never changes. If you need to alter the schema, create V2__.sql

CREATE TABLE tenants (
                         id            VARCHAR(100)   PRIMARY KEY,
                         name          VARCHAR(255)   NOT NULL,
                         subdomain     VARCHAR(100)   NOT NULL UNIQUE,
                         status        VARCHAR(50)    NOT NULL DEFAULT 'TRIAL',
                         plan_type     VARCHAR(50)    NOT NULL DEFAULT 'STARTER',
                         contact_email VARCHAR(255),
                         timezone      VARCHAR(100)   NOT NULL DEFAULT 'Asia/Kolkata',
                         currency      VARCHAR(10)    NOT NULL DEFAULT 'INR',
                         created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE staff (
                       id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid()::text,
                       tenant_id     VARCHAR(100)   NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       name          VARCHAR(255)   NOT NULL,
                       email         VARCHAR(255)   NOT NULL,
                       password_hash VARCHAR(255)   NOT NULL,
                       role          VARCHAR(50)    NOT NULL,
                       active        BOOLEAN        NOT NULL DEFAULT TRUE,
                       phone_number  VARCHAR(20),
                       created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                       UNIQUE (tenant_id, email)   -- same email allowed in different tenants
);

CREATE TABLE menu_items (
                            id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid()::text,
                            tenant_id         VARCHAR(100)   NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                            name              VARCHAR(255)   NOT NULL,
                            description       TEXT,
                            price             NUMERIC(10,2)  NOT NULL CHECK (price >= 0),
                            category          VARCHAR(50),
                            available         BOOLEAN        NOT NULL DEFAULT TRUE,
                            prep_time_minutes INT,
                            image_url         TEXT,
                            created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                            updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
                        id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid()::text,
                        tenant_id      VARCHAR(100)   NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                        table_number   VARCHAR(20),
                        customer_name  VARCHAR(255),
                        status         VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
                        total_amount   NUMERIC(10,2)  NOT NULL DEFAULT 0,
                        staff_id       VARCHAR(255)           REFERENCES staff(id) ON DELETE SET NULL,
                        notes          TEXT,
                        created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                        updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
                             id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid()::text,
                             order_id      VARCHAR(255)           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             menu_item_id  VARCHAR(255)           NOT NULL REFERENCES menu_items(id),
                             quantity      INT            NOT NULL CHECK (quantity > 0),
                             unit_price    NUMERIC(10,2)  NOT NULL,
                             subtotal      NUMERIC(10,2)  NOT NULL
);

-- Every high-frequency query goes through tenant_id. These indexes are not optional.
CREATE INDEX idx_staff_tenant      ON staff(tenant_id);
CREATE INDEX idx_menu_tenant       ON menu_items(tenant_id);
CREATE INDEX idx_menu_category     ON menu_items(tenant_id, category);
CREATE INDEX idx_orders_tenant     ON orders(tenant_id);
CREATE INDEX idx_orders_status     ON orders(tenant_id, status);
CREATE INDEX idx_orders_created    ON orders(tenant_id, created_at DESC);