package repository

import (
	"context"
	"errors"
	"fmt"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"main/model"
)

const (
	selectBids = "SELECT b.id, b.name, b.description, b.status, b.version, b.created_at, b.author_type, b.author_id, b.tender_id, b.bid_review_id FROM bids b "
)

func getBidsQuery(query string, page Page) string {
	return selectBids + query + fmt.Sprintf(orderLimitOffset, page.Limit, page.Offset)
}

func FindAllBids(db *pgxpool.Pool, page Page) ([]model.Bid, error) {
	rows, err := db.Query(context.Background(), getBidsQuery("", page))
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	bids := []model.Bid{}
	for rows.Next() {
		var b model.Bid
		if err := rows.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
			return nil, err
		}
		bids = append(bids, b)
	}
	return bids, rows.Err()
}

func StoreBid(db *pgxpool.Pool, bid model.Bid) (*model.Bid, error) {
	row := db.QueryRow(context.Background(), "INSERT INTO bids (name, description, status, author_type, author_id) VALUES ($1, $2, $3, $4, $5) RETURNING id, name, description, status, version, created_at, author_type, author_id, tender_id, bid_review_id",
		bid.Name, bid.Description, bid.Status, bid.AuthorType, bid.AuthorId)
	var b model.Bid
	if err := row.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
		return nil, err
	}
	return &b, nil
}

func FindBidsByUsername(db *pgxpool.Pool, username string, page Page) ([]model.Bid, error) {
	rows, err := db.Query(context.Background(), getBidsQuery(" LEFT JOIN employee e1 ON e1.username = $1 LEFT JOIN organization_responsible r ON r.user_id = e1.id LEFT JOIN organization o ON o.id = b.author_id WHERE (b.author_type = 'Organization' and b.author_id = r.organization_id) OR (b.author_type = 'User' and b.author_id = e1.id)", page), username)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	bids := []model.Bid{}
	for rows.Next() {
		var b model.Bid
		if err := rows.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
			return nil, err
		}
		bids = append(bids, b)
	}
	return bids, rows.Err()
}

func FindBidsByTender(db *pgxpool.Pool, tenderId uuid.UUID, username string, page Page) ([]model.Bid, error) {
	_, err := userIsTenderCreatorById(db, tenderId, username)
	if err != nil {
		return nil, err
	}
	rows, err := db.Query(context.Background(), getBidsQuery(" WHERE b.tender_id = $1", page), tenderId)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	bids := []model.Bid{}
	for rows.Next() {
		var b model.Bid
		if err := rows.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
			return nil, err
		}
		bids = append(bids, b)
	}
	return bids, rows.Err()
}

func userIsTenderCreatorById(db *pgxpool.Pool, tenderId uuid.UUID, username string) (*model.Employee, error) {
	tender, err := FindTenderById(db, tenderId)
	if err != nil {
		return nil, err
	}
	employee, _, err := userIsTenderCreator(db, *tender, username)
	if err != nil {
		return nil, err
	}
	return employee, err
}

func userIsBidCreator(db *pgxpool.Pool, bid model.Bid, username string) (*model.Employee, error) {
	creator, err := FindEmployeeByUsername(db, username)
	if err != nil {
		return nil, errors.New("user does not exist")
	}
	if bid.AuthorType == "User" {
		if bid.AuthorId != creator.Id {
			return nil, errors.New("bid does not belong to the user")
		}
	} else {
		organization, err := FindOrganizationByEmployeeId(db, creator.Id)
		if err != nil {
			return nil, errors.New("user is not a responsible")
		}
		if bid.AuthorId != organization.Id {
			return nil, errors.New("bid does not belong to the user")
		}
	}
	return creator, nil
}

func FindBidById(db *pgxpool.Pool, id uuid.UUID) (*model.Bid, error) {
	row := db.QueryRow(context.Background(), selectBids+" WHERE b.id = $1", id)
	var b model.Bid
	if err := row.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
		return nil, err
	}
	return &b, nil
}

func FindBidByIdAndUserAccess(db *pgxpool.Pool, id uuid.UUID, username string) (*model.Bid, error) {
	b, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	_, err1 := userIsBidCreator(db, *b, username)
	if _, err2 := userIsTenderCreatorById(db, b.TenderId, username); err1 != nil && err2 != nil {
		return nil, err1
	}
	return b, nil
}

func SetBidStatusById(db *pgxpool.Pool, id uuid.UUID, username string, status string) (*model.Bid, error) {
	bid, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	_, err = userIsBidCreator(db, *bid, username)
	if err != nil {
		return nil, err
	}
	var b model.Bid
	row := db.QueryRow(context.Background(), "UPDATE bids SET status = $1 WHERE id = $2 RETURNING id, name, description, status, version, created_at, author_type, author_id, tender_id, bid_review_id", status, id)
	if err := row.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
		return nil, err
	}
	return &b, nil
}

func UpdateBidById(db *pgxpool.Pool, id uuid.UUID, username string, bid model.Bid) (*model.Bid, error) {
	old, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	_, err = userIsBidCreator(db, *old, username)
	if err != nil {
		return nil, err
	}
	db.QueryRow(context.Background(), "INSERT INTO bid_rev (name, description, bid_id, version) SELECT name, description, id, version FROM tenders WHERE id = $1", id)

	var query = "UPDATE bids SET"
	if old.Name != bid.Name && bid.Name != "" {
		query += fmt.Sprintf(" name = '%s',", bid.Name)
	}
	if old.Description != bid.Description && bid.Description != "" {
		query += fmt.Sprintf(" description = '%s',", bid.Description)
	}
	query += " version = version+1 WHERE id = $1 RETURNING id, name, description, status, version, created_at, author_type, author_id, tender_id, bid_review_id"

	row := db.QueryRow(context.Background(), query, id)
	var b model.Bid
	if err := row.Scan(&b.Id, &b.Name, &b.Description, &b.Status, &b.Version, &b.CreatedAt, &b.AuthorType, &b.AuthorId, &b.TenderId, &b.BidReviewId); err != nil {
		return nil, err
	}
	return &b, nil
}

func RollbackBidById(db *pgxpool.Pool, id uuid.UUID, version int, username string) (*model.Bid, error) {
	old, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	_, err = userIsBidCreator(db, *old, username)
	if err != nil {
		return nil, err
	}
	db.QueryRow(context.Background(), "INSERT INTO bid_rev (name, description, bid_id, version) SELECT name, description, id, version FROM tenders WHERE id = $1", id)
	row := db.QueryRow(context.Background(), "SELECT name, description FROM bid_rev WHERE bid_id = $1 AND version = $2", id, version)
	var backup model.Bid
	if err := row.Scan(&backup.Id, &backup.Name, &backup.Description, &backup.Status, &backup.Version, &backup.CreatedAt, &backup.AuthorType, &backup.AuthorId, &backup.TenderId, &backup.BidReviewId); err != nil {
		return nil, errors.New("no such version for tender")
	}
	row = db.QueryRow(context.Background(), "UPDATE bids SET (name, description) = ($1, $2), version = version+1 WHERE id = $4 RETURNING id, name, description, status, version, created_at, author_type, author_id, tender_id, bid_review_id", backup.Name, backup.Description, id)
	var b model.Bid
	if err := row.Scan(&backup.Id, &backup.Name, &backup.Description, &backup.Status, &backup.Version, &backup.CreatedAt, &backup.AuthorType, &backup.AuthorId, &backup.TenderId, &backup.BidReviewId); err != nil {
		return nil, err
	}
	return &b, nil
}

func SubmitDecision(db *pgxpool.Pool, id uuid.UUID, username string, decision string) (*model.Bid, error) {
	old, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	user, err := userIsTenderCreatorById(db, old.TenderId, username)
	if err != nil {
		return nil, err
	}
	if old.Status != model.BidStatusPublished {
		return nil, errors.New("bid status is not published")
	}
	if decision == model.BidStatusApproved {
		row := db.QueryRow(context.Background(), "SELECT id, organization_id FROM organization_responsible WHERE user_id = $1", user.Id)
		var resp, org uuid.UUID
		if err := row.Scan(&resp, &org); err != nil {
			return nil, err
		}
		db.QueryRow(context.Background(), "INSERT INTO bids_approvers (bid_id, responsible_id) VALUES ($1, $2)", id, resp)
		row = db.QueryRow(context.Background(), "SELECT COUNT(*) FROM bids_approvers b WHERE b.bid_id = $1", id)
		var nresp int
		if err := row.Scan(&nresp); err != nil {
			return nil, err
		}

		row = db.QueryRow(context.Background(), "SELECT COUNT(*) as total FROM organization_responsible r WHERE r.organization_id = $1", org)
		var total int
		if err := row.Scan(&total); err != nil {
			return nil, err
		}

		if nresp > 3 || nresp > total {
			db.QueryRow(context.Background(), "UPDATE bids SET status = 'Approved' WHERE id = $1", id)
		}
	} else {
		db.QueryRow(context.Background(), "UPDATE bids SET status = 'Rejected' WHERE id = $1", id)
	}
	bid, err := FindBidById(db, id)
	if err != nil {
		return nil, err
	}
	return bid, nil
}
