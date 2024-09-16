package model

import (
	"github.com/google/uuid"
	"time"
)

type Tender struct {
	Id              uuid.UUID `json:"id"`
	Name            string    `json:"name"`
	Description     string    `json:"description"`
	Status          string    `json:"status"`
	ServiceType     string    `json:"serviceType"`
	CreatorUsername string    `json:"creatorUsername"`
	OrganizationId  uuid.UUID `json:"organizationId"`
	Version         int       `json:"version"`
	CreatedAt       time.Time `json:"createdAt"`
	bids            []Bid
}

func NewTender(name string, description string, serviceType string, creatorUsername string) *Tender {
	return &Tender{Id: uuid.New(), Name: name, Description: description, Status: "Created", ServiceType: serviceType, CreatorUsername: creatorUsername, Version: 1, CreatedAt: time.Now(), bids: []Bid{}}
}

const (
	TenderStatusCreated   = "Created"
	TenderStatusPublished = "Published"
	TenderStatusClosed    = "Closed"
)
