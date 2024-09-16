package routes

import (
	"github.com/gin-gonic/gin"
	"main/controller"
)

func InitRoutes(r *gin.Engine) {
	api := r.Group("/api")

	api.GET("/ping", controller.Ping)

	tenders := api.Group("/tenders")
	tenders.GET("/all", controller.GetAllTenders)
	tenders.GET("", controller.GetTenders)
	tenders.POST("/new", controller.PostTender)
	tenders.GET("/my", controller.GetMyTenders)
	tenders.GET("/:id/status", controller.GetTenderStatus)
	tenders.PUT("/:id/status", controller.PutTenderStatus)
	tenders.PATCH("/:id/edit", controller.PatchTender)
	tenders.PUT("/:id/rollback/:version", controller.RollbackTender)
}
