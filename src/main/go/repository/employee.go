package repository

import (
	"context"
	"github.com/jackc/pgx/v5/pgxpool"
	"log"
	"main/model"
)

func FindEmployeeByUsername(db *pgxpool.Pool, username string) (*model.Employee, error) {
	row := db.QueryRow(context.Background(), "select id, username from employee where username = $1", username)
	var e model.Employee
	if err := row.Scan(&e.Id, &e.Username); err != nil {
		log.Println(err)
		return nil, err
	}
	return &e, nil
}
