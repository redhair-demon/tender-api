package model

import (
	"github.com/google/uuid"
	"time"
)

type BidReview struct {
	Id          uuid.UUID `json:"id"`
	Description string    `json:"description"`
	CreatedAt   time.Time `json:"createdAt"`
}

type Organization struct {
	Id          uuid.UUID `json:"id"`
	Name        string    `json:"name"`
	Description string    `json:"description"`
	Type        string    `json:"type"`
	CreatedAt   time.Time `json:"createdAt"`
	UpdatedAt   time.Time `json:"updatedAt"`
}

const (
	LLC = "LLC"
	IE  = "IE"
	JSC = "JSC"
)
