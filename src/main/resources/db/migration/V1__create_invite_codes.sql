CREATE TABLE invite_codes (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                       VARCHAR(8) NOT NULL UNIQUE,
    handed_out_at              TIMESTAMPTZ,
    first_redeemed_at          TIMESTAMPTZ,
    window_expires_at          TIMESTAMPTZ,
    usage_budget_seconds       INT NOT NULL DEFAULT 1200,
    usage_consumed_seconds     INT NOT NULL DEFAULT 0,
    failed_validation_attempts INT NOT NULL DEFAULT 0,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at                 TIMESTAMPTZ
);

CREATE INDEX idx_invite_codes_code ON invite_codes(code);
CREATE INDEX idx_invite_codes_handed_out_at ON invite_codes(handed_out_at) WHERE handed_out_at IS NULL;
