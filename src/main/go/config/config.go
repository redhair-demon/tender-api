package config

import (
	"context"
	"fmt"
	"github.com/jackc/pgx/v5/pgxpool"
	"os"
)

var DB *pgxpool.Pool

func InitDB() {
	var err error
	DB, err = pgxpool.New(context.Background(), os.Getenv("POSTGRES_CONN"))
	if err != nil {
		panic(err)
	}
	if err := DB.Ping(context.Background()); err != nil {
		panic(err)
	}
	fmt.Println("PostgreSQL connection established")
}
