package repository

import (
	"context"
	"errors"
	"fmt"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"log"
	"main/model"
)

const (
	selectTender     = "SELECT t.id, t.name, t.description, t.status, t.service_type, t.version, t.created_at, t.organization_id FROM tenders t"
	orderLimitOffset = " ORDER BY t.name ASC LIMIT %d OFFSET %d"
)

type Page struct {
	Limit  int `json:"limit"`
	Offset int `json:"offset"`
}

func getTendersQuery(query string, page Page) string {
	return selectTender + query + fmt.Sprintf(orderLimitOffset, page.Limit, page.Offset)
}

func userIsTenderCreator(db *pgxpool.Pool, tender model.Tender, username string) (*model.Employee, *model.Organization, error) {
	creator, err := FindEmployeeByUsername(db, username)
	if err != nil {
		return nil, nil, errors.New("user does not exist")
	}
	organization, err := FindOrganizationByEmployeeId(db, creator.Id)
	if err != nil {
		return nil, nil, errors.New("user is not a responsible")
	}
	if organization.Id != tender.OrganizationId {
		return nil, nil, errors.New("user does not belong to this organization")
	}
	return creator, organization, nil
}

func FindAllTenders(db *pgxpool.Pool, page Page) ([]model.Tender, error) {
	rows, err := db.Query(context.Background(), getTendersQuery("", page))
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	tenders := []model.Tender{}
	for rows.Next() {
		var t model.Tender
		if err := rows.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
			log.Println(err)
			return nil, err
		}
		tenders = append(tenders, t)
	}
	return tenders, rows.Err()
}

func FindPublishedTenders(db *pgxpool.Pool, serviceType []string, page Page) ([]model.Tender, error) {
	var query string
	var rows pgx.Rows
	var err error
	if len(serviceType) == 0 {
		query = getTendersQuery(" WHERE t.status = 'Published'", page)
		rows, err = db.Query(context.Background(), query)
	} else {
		query = getTendersQuery(" WHERE t.status = 'Published' AND t.service_type = ANY ($1)", page)
		rows, err = db.Query(context.Background(), query, serviceType)
	}
	fmt.Println(query)

	if err != nil {
		log.Println(err)
		return nil, err
	}
	defer rows.Close()
	tenders := []model.Tender{}
	for rows.Next() {
		var t model.Tender
		if err := rows.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
			log.Println(err)
			return nil, err
		}
		tenders = append(tenders, t)
	}
	return tenders, rows.Err()
}

func FindTenderById(db *pgxpool.Pool, id uuid.UUID) (*model.Tender, error) {
	row := db.QueryRow(context.Background(), selectTender+" WHERE t.id = $1", id)
	var t model.Tender
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		return nil, err
	}
	return &t, nil
}

func FindTenderByIdAndUserAccess(db *pgxpool.Pool, id uuid.UUID, username string) (*model.Tender, error) {
	row := db.QueryRow(context.Background(), selectTender+" WHERE t.id = $1", id)
	var t model.Tender
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		return nil, err
	}
	if _, _, err := userIsTenderCreator(db, t, username); err != nil && t.Status != model.TenderStatusPublished {
		return nil, err
	}
	return &t, nil
}

func FindTendersByUsername(db *pgxpool.Pool, username string, page Page) ([]model.Tender, error) {
	rows, err := db.Query(context.Background(), getTendersQuery(" JOIN employee e1 ON e1.username = t.creator_username JOIN organization_responsible r1 on r1.user_id = e1.id JOIN employee e2 ON e2.username = $1 JOIN organization_responsible r2 ON r2.user_id = e2.id WHERE r1.organization_id = r2.organization_id", page), username)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	tenders := []model.Tender{}
	for rows.Next() {
		var t model.Tender
		if err := rows.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
			log.Println(err)
			return nil, err
		}
		tenders = append(tenders, t)
	}
	return tenders, rows.Err()
}

func StoreTender(db *pgxpool.Pool, tender model.Tender) (*model.Tender, error) {
	creator, organization, err := userIsTenderCreator(db, tender, tender.CreatorUsername)
	if err != nil {
		return nil, err
	}
	row := db.QueryRow(context.Background(), "INSERT INTO tenders (id, name, description, status, service_type, version, created_at, creator_username, organization_id, creator_id) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id, name, description, status, service_type, version, created_at",
		tender.Id, tender.Name, tender.Description, tender.Status, tender.ServiceType, tender.Version, tender.CreatedAt, tender.CreatorUsername, organization.Id, creator.Id)
	var t model.Tender
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		log.Println(err)
		return nil, err
	}
	return &t, nil
}

func SetTenderStatusById(db *pgxpool.Pool, id uuid.UUID, username string, status string) (*model.Tender, error) {
	tender, err := FindTenderById(db, id)
	if err != nil {
		return nil, err
	}
	_, _, err = userIsTenderCreator(db, *tender, username)
	if err != nil {
		return nil, err
	}
	var t model.Tender
	row := db.QueryRow(context.Background(), "UPDATE tenders SET status = $1 WHERE id = $2 RETURNING id, name, description, status, service_type, version, created_at, organization_id", status, id)
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		return nil, err
	}
	return &t, nil
}

func UpdateTenderById(db *pgxpool.Pool, id uuid.UUID, username string, tender model.Tender) (*model.Tender, error) {
	old, err := FindTenderById(db, id)
	if err != nil {
		return nil, err
	}
	_, _, err = userIsTenderCreator(db, *old, username)
	if err != nil {
		return nil, err
	}
	db.QueryRow(context.Background(), "INSERT INTO tenders_rev (name, description, service_type, tender_id, version) SELECT name, description, service_type, id, version FROM tenders WHERE id = $1", id)

	var query = "UPDATE tenders SET"
	if old.Name != tender.Name && tender.Name != "" {
		query += fmt.Sprintf(" name = '%s',", tender.Name)
	}
	if old.Description != tender.Description && tender.Description != "" {
		query += fmt.Sprintf(" description = '%s',", tender.Description)
	}
	if old.ServiceType != tender.ServiceType && tender.ServiceType != "" {
		query += fmt.Sprintf(" service_type = '%s',", tender.ServiceType)
	}
	query += " version = version+1 WHERE id = $1 RETURNING id, name, description, status, service_type, version, created_at, organization_id"

	row := db.QueryRow(context.Background(), query, id)
	var t model.Tender
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		return nil, err
	}
	return &t, nil
}

func RollbackTenderById(db *pgxpool.Pool, id uuid.UUID, version int, username string) (*model.Tender, error) {
	old, err := FindTenderById(db, id)
	if err != nil {
		return nil, err
	}
	_, _, err = userIsTenderCreator(db, *old, username)
	if err != nil {
		return nil, err
	}
	db.QueryRow(context.Background(), "INSERT INTO tenders_rev (name, description, service_type, tender_id, version) SELECT name, description, service_type, id, version FROM tenders WHERE id = $1", id)
	row := db.QueryRow(context.Background(), "SELECT name, description, service_type FROM tenders_rev WHERE tender_id = $1 AND version = $2", id, version)
	var backup model.Tender
	if err := row.Scan(&backup.Id, &backup.Name, &backup.Description, &backup.Status, &backup.ServiceType, &backup.Version, &backup.CreatedAt, &backup.OrganizationId); err != nil {
		return nil, errors.New("no such version for tender")
	}
	row = db.QueryRow(context.Background(), "UPDATE tenders SET (name, description, service_type) = ($1, $2, $3), version = version+1 WHERE id = $4 RETURNING id, name, description, status, service_type, version, created_at, organization_id", backup.Name, backup.Description, backup.ServiceType, id)
	var t model.Tender
	if err := row.Scan(&t.Id, &t.Name, &t.Description, &t.Status, &t.ServiceType, &t.Version, &t.CreatedAt, &t.OrganizationId); err != nil {
		return nil, err
	}
	return &t, nil
}
