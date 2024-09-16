package model

import (
	"github.com/google/uuid"
	"time"
)

type Bid struct {
	Id         uuid.UUID `json:"id"`
	Name       string    `json:"name"`
	Status     string    `json:"status"`
	AuthorType string    `json:"authorType"`
	AuthorId   uuid.UUID `json:"authorId"`
	Version    int       `json:"version"`
	CreatedAt  time.Time `json:"createdAt"`
}
