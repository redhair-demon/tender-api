package routes

import (
	"github.com/gin-gonic/gin"
	"main/controller"
)

func InitRoutes(r *gin.Engine) {
	api := r.Group("/api")

	api.GET("/ping", controller.Ping)

	tenders := api.Group("/tenders")
	tenders.GET("/all", controller.GetAllTenders) //GET all from db for development
	tenders.GET("", controller.GetTenders)
	tenders.POST("/new", controller.PostTender)
	tenders.GET("/my", controller.GetMyTenders)
	tenders.GET("/:id/status", controller.GetTenderStatus)
	tenders.PUT("/:id/status", controller.PutTenderStatus)
	tenders.PATCH("/:id/edit", controller.PatchTender)
	tenders.PUT("/:id/rollback/:version", controller.RollbackTender)

	bids := api.Group("/bids")
	bids.GET("/all", controller.GetAllBids) // GET all from db for development
	bids.POST("/new", controller.PostBid)
	bids.GET("/my", controller.GetMyBids)
	bids.GET("/:tenderId/list", controller.GetBidsByTenderId)
	bids.GET("/:id/status", controller.GetBidStatus)
	bids.PUT("/:id/status", controller.PutBidStatus)
	bids.PATCH("/:id/edit", controller.PatchBid)
	bids.PUT("/:id/submit_decision", controller.SubmitDecision)
	bids.PUT("/:id/feedback")
	bids.PUT("/:id/rollback/:version", controller.RollbackBid)
	bids.GET("/:tenderId/reviews")
}
