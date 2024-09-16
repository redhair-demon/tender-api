package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"main/config"
	"main/routes"
	"os"
)

func main() {
	config.InitDB()
	router := gin.Default()
	routes.InitRoutes(router)

	serverAddress := os.Getenv("SERVER_ADDRESS")
	fmt.Printf("Server address: %s\n", serverAddress)
	router.Run(serverAddress)
}
