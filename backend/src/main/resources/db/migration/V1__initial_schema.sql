-- ============================================================
-- Yatrify Database Schema - V1 Initial Migration
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- USER PROFILES (Yatrify-specific extension of generic users)
-- ============================================================
CREATE TABLE user_profiles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    generic_user_id     VARCHAR(255) NOT NULL UNIQUE,  -- ID from generic platform
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(20),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100),
    profile_picture_url TEXT,
    date_of_birth       DATE,
    gender              VARCHAR(20),
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    city                VARCHAR(100),
    state               VARCHAR(100),
    country             VARCHAR(100) DEFAULT 'India',
    pincode             VARCHAR(20),
    emergency_contact_name  VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    preferred_language  VARCHAR(20) DEFAULT 'en',
    bio                 TEXT,
    accessibility_needs TEXT,
    travel_preferences  JSONB DEFAULT '[]',
    is_verified         BOOLEAN DEFAULT false,
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    onboarding_completed BOOLEAN DEFAULT false,
    is_active           BOOLEAN DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

-- ============================================================
-- ORGANIZER PROFILES
-- ============================================================
CREATE TABLE organizer_profiles (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id         UUID NOT NULL REFERENCES user_profiles(id),
    organization_name       VARCHAR(255) NOT NULL,
    organization_type       VARCHAR(50) NOT NULL,  -- INDIVIDUAL, AGENCY, NGO, TEMPLE_TRUST
    gstin                   VARCHAR(20),
    pan_number              VARCHAR(20),
    website                 VARCHAR(255),
    description             TEXT,
    logo_url                TEXT,
    cover_image_url         TEXT,
    bank_account_number     VARCHAR(50),
    bank_ifsc_code          VARCHAR(20),
    bank_name               VARCHAR(100),
    bank_account_holder     VARCHAR(255),
    is_verified             BOOLEAN DEFAULT false,
    verification_status     VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, UNDER_REVIEW, VERIFIED, REJECTED
    verification_notes      TEXT,
    rating                  DECIMAL(3,2) DEFAULT 0.00,
    total_reviews           INTEGER DEFAULT 0,
    total_trips_conducted   INTEGER DEFAULT 0,
    is_active               BOOLEAN DEFAULT true,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

-- ============================================================
-- VERIFICATION DOCUMENTS
-- ============================================================
CREATE TABLE verification_documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID REFERENCES user_profiles(id),
    organizer_id    UUID REFERENCES organizer_profiles(id),
    document_type   VARCHAR(100) NOT NULL,  -- AADHAAR, PAN, PASSPORT, GSTIN, TRADE_LICENSE
    document_number VARCHAR(255),
    document_url    TEXT NOT NULL,
    back_side_url   TEXT,
    status          VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, VERIFIED, REJECTED
    rejection_reason TEXT,
    verified_by     VARCHAR(255),
    verified_at     TIMESTAMP,
    expires_at      DATE,
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TRIPS
-- ============================================================
CREATE TABLE trips (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organizer_id            UUID NOT NULL REFERENCES organizer_profiles(id),
    title                   VARCHAR(500) NOT NULL,
    slug                    VARCHAR(500) UNIQUE,
    description             TEXT,
    short_description       VARCHAR(500),
    trip_type               VARCHAR(50) NOT NULL,  -- RELIGIOUS, HONEYMOON, BACHELOR, FAMILY, ADVENTURE, SOLO
    sub_type                VARCHAR(100),
    destinations            JSONB DEFAULT '[]',   -- list of cities/places
    cover_image_url         TEXT,
    gallery_images          JSONB DEFAULT '[]',
    duration_days           INTEGER NOT NULL,
    duration_nights         INTEGER NOT NULL,
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    registration_deadline   TIMESTAMP NOT NULL,
    departure_city          VARCHAR(255),
    departure_location      TEXT,
    total_seats             INTEGER NOT NULL,
    available_seats         INTEGER NOT NULL,
    min_seats               INTEGER DEFAULT 1,
    price_per_person        DECIMAL(12,2) NOT NULL,
    base_price              DECIMAL(12,2) NOT NULL,
    child_price             DECIMAL(12,2),
    infant_price            DECIMAL(12,2) DEFAULT 0,
    discount_percentage     DECIMAL(5,2) DEFAULT 0,
    gst_percentage          DECIMAL(5,2) DEFAULT 5.00,
    currency                VARCHAR(10) DEFAULT 'INR',
    inclusions              JSONB DEFAULT '[]',
    exclusions              JSONB DEFAULT '[]',
    terms_and_conditions    TEXT,
    cancellation_policy     TEXT,
    highlights              JSONB DEFAULT '[]',
    languages_spoken        JSONB DEFAULT '["Hindi", "English"]',
    difficulty_level        VARCHAR(50) DEFAULT 'EASY',
    age_restriction_min     INTEGER DEFAULT 0,
    age_restriction_max     INTEGER DEFAULT 100,
    is_visa_required        BOOLEAN DEFAULT false,
    is_international        BOOLEAN DEFAULT false,
    status                  VARCHAR(50) DEFAULT 'DRAFT',  -- DRAFT, PUBLISHED, SOLD_OUT, CANCELLED, COMPLETED
    is_featured             BOOLEAN DEFAULT false,
    is_trending             BOOLEAN DEFAULT false,
    rating                  DECIMAL(3,2) DEFAULT 0.00,
    total_reviews           INTEGER DEFAULT 0,
    total_bookings          INTEGER DEFAULT 0,
    view_count              INTEGER DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

-- ============================================================
-- TRIP ITINERARY (Day-wise plan)
-- ============================================================
CREATE TABLE trip_itineraries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id         UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    day_number      INTEGER NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    activities      JSONB DEFAULT '[]',
    meals           JSONB DEFAULT '{}',   -- {breakfast: true, lunch: true, dinner: true}
    accommodation   VARCHAR(255),
    transport       VARCHAR(255),
    image_url       TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(trip_id, day_number)
);

-- ============================================================
-- TRIP HOTELS
-- ============================================================
CREATE TABLE trip_hotels (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id     UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    hotel_name  VARCHAR(255) NOT NULL,
    city        VARCHAR(100),
    star_rating INTEGER,
    check_in_day    INTEGER,
    check_out_day   INTEGER,
    room_type   VARCHAR(100),
    image_url   TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_reference   VARCHAR(50) NOT NULL UNIQUE,
    trip_id             UUID NOT NULL REFERENCES trips(id),
    user_profile_id     UUID NOT NULL REFERENCES user_profiles(id),
    organizer_id        UUID NOT NULL REFERENCES organizer_profiles(id),
    booking_type        VARCHAR(50) DEFAULT 'ONLINE',  -- ONLINE, OFFLINE
    status              VARCHAR(50) DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, COMPLETED, REFUNDED
    num_adults          INTEGER NOT NULL DEFAULT 1,
    num_children        INTEGER DEFAULT 0,
    num_infants         INTEGER DEFAULT 0,
    total_travelers     INTEGER NOT NULL,
    base_amount         DECIMAL(12,2) NOT NULL,
    discount_amount     DECIMAL(12,2) DEFAULT 0,
    gst_amount          DECIMAL(12,2) DEFAULT 0,
    total_amount        DECIMAL(12,2) NOT NULL,
    amount_paid         DECIMAL(12,2) DEFAULT 0,
    refund_amount       DECIMAL(12,2) DEFAULT 0,
    special_requests    TEXT,
    cancellation_reason TEXT,
    cancelled_at        TIMESTAMP,
    confirmed_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

-- ============================================================
-- BOOKING TRAVELERS (individual traveler details)
-- ============================================================
CREATE TABLE booking_travelers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100),
    date_of_birth   DATE,
    gender          VARCHAR(20),
    traveler_type   VARCHAR(20) DEFAULT 'ADULT',  -- ADULT, CHILD, INFANT
    id_type         VARCHAR(50),   -- AADHAAR, PAN, PASSPORT
    id_number       VARCHAR(100),
    passport_number VARCHAR(50),
    passport_expiry DATE,
    nationality     VARCHAR(100) DEFAULT 'Indian',
    dietary_preference VARCHAR(100),
    medical_conditions TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id          UUID NOT NULL REFERENCES bookings(id),
    payment_reference   VARCHAR(255) NOT NULL UNIQUE,
    gateway_order_id    VARCHAR(255),
    gateway_payment_id  VARCHAR(255),
    gateway_signature   VARCHAR(500),
    payment_gateway     VARCHAR(50) DEFAULT 'RAZORPAY',
    payment_method      VARCHAR(100),  -- UPI, CARD, NETBANKING, WALLET
    amount              DECIMAL(12,2) NOT NULL,
    currency            VARCHAR(10) DEFAULT 'INR',
    status              VARCHAR(50) DEFAULT 'INITIATED',  -- INITIATED, SUCCESS, FAILED, REFUNDED
    failure_reason      TEXT,
    refund_id           VARCHAR(255),
    refunded_at         TIMESTAMP,
    paid_at             TIMESTAMP,
    metadata            JSONB DEFAULT '{}',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- WISHLISTS
-- ============================================================
CREATE TABLE wishlists (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    trip_id         UUID NOT NULL REFERENCES trips(id),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_profile_id, trip_id)
);

-- ============================================================
-- REVIEWS
-- ============================================================
CREATE TABLE reviews (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id         UUID NOT NULL REFERENCES trips(id),
    booking_id      UUID REFERENCES bookings(id),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    rating          INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title           VARCHAR(255),
    content         TEXT,
    images          JSONB DEFAULT '[]',
    value_rating    INTEGER CHECK (value_rating >= 1 AND value_rating <= 5),
    hospitality_rating  INTEGER CHECK (hospitality_rating >= 1 AND hospitality_rating <= 5),
    transport_rating    INTEGER CHECK (transport_rating >= 1 AND transport_rating <= 5),
    hotel_rating    INTEGER CHECK (hotel_rating >= 1 AND hotel_rating <= 5),
    is_verified     BOOLEAN DEFAULT false,
    organizer_response  TEXT,
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    title           VARCHAR(255) NOT NULL,
    body            TEXT NOT NULL,
    type            VARCHAR(100) NOT NULL,  -- BOOKING_CONFIRMED, BOOKING_CANCELLED, TRIP_REMINDER, etc.
    category        VARCHAR(50),  -- BOOKING, PAYMENT, TRIP, ACCOUNT
    reference_id    UUID,
    reference_type  VARCHAR(100),
    is_read         BOOLEAN DEFAULT false,
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- AI CHAT SESSIONS
-- ============================================================
CREATE TABLE ai_chat_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID REFERENCES user_profiles(id),
    session_type    VARCHAR(50) DEFAULT 'USER',  -- USER, ORGANIZER
    context         JSONB DEFAULT '{}',
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_chat_messages (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id  UUID NOT NULL REFERENCES ai_chat_sessions(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL,  -- USER, ASSISTANT
    content     TEXT NOT NULL,
    metadata    JSONB DEFAULT '{}',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_user_profiles_generic_user_id ON user_profiles(generic_user_id);
CREATE INDEX idx_user_profiles_email ON user_profiles(email);
CREATE INDEX idx_organizer_profiles_user_profile_id ON organizer_profiles(user_profile_id);
CREATE INDEX idx_organizer_profiles_verification_status ON organizer_profiles(verification_status);
CREATE INDEX idx_trips_organizer_id ON trips(organizer_id);
CREATE INDEX idx_trips_trip_type ON trips(trip_type);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_start_date ON trips(start_date);
CREATE INDEX idx_trips_slug ON trips(slug);
CREATE INDEX idx_trips_is_featured ON trips(is_featured);
CREATE INDEX idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX idx_bookings_user_profile_id ON bookings(user_profile_id);
CREATE INDEX idx_bookings_booking_reference ON bookings(booking_reference);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_wishlists_user_profile_id ON wishlists(user_profile_id);
CREATE INDEX idx_reviews_trip_id ON reviews(trip_id);
CREATE INDEX idx_notifications_user_profile_id ON notifications(user_profile_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- Full text search index for trips
CREATE INDEX idx_trips_title_fts ON trips USING gin(to_tsvector('english', title));
CREATE INDEX idx_trips_description_fts ON trips USING gin(to_tsvector('english', coalesce(description, '')));
