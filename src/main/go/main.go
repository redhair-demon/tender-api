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

//func initializeRoutes(h *apphttp.NoteHandler) http.Handler {
//	mux := http.NewServeMux()
//	mux.HandleFunc("GET /api/notes", h.GetAll)
//	mux.HandleFunc("GET /api/notes/{id}", h.Get)
//	mux.HandleFunc("POST /api/notes", h.Post)
//	mux.HandleFunc("PUT /api/notes/{id}", h.Put)
//	mux.HandleFunc("DELETE /api/notes/{id}", h.Delete)
//	return mux
//}
