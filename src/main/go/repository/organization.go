package repository

import (
	"context"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"log"
	"main/model"
)

func FindOrganizationByEmployeeId(db *pgxpool.Pool, employeeId uuid.UUID) (*model.Organization, error) {
	row := db.QueryRow(context.Background(), "SELECT organization.id FROM organization JOIN public.organization_responsible o on organization.id = o.organization_id WHERE o.user_id = $1", employeeId)
	var o model.Organization
	if err := row.Scan(&o.Id); err != nil {
		log.Println(err)
		return nil, err
	}
	return &o, nil
}
