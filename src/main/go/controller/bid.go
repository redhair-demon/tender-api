package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"main/config"
	"main/model"
	"main/repository"
	"net/http"
	"strconv"
)

func GetAllBids(c *gin.Context) {
	page := queryPage(c)
	bids, err := repository.FindAllBids(config.DB, page)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bids)
}

func PostBid(c *gin.Context) {
	var body model.Bid

	if err := c.BindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	createdBid, err := repository.StoreBid(config.DB, body)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, createdBid)
}

func GetMyBids(c *gin.Context) {
	page := queryPage(c)
	username := c.Query("username")

	bids, err := repository.FindBidsByUsername(config.DB, username, page)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bids)
}

func GetBidsByTenderId(c *gin.Context) {
	page := queryPage(c)
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
	}
	tenderId, err := uuid.Parse(c.Param("tenderId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	bids, err := repository.FindBidsByTender(config.DB, tenderId, username, page)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bids)
}

func GetBidStatus(c *gin.Context) {
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
	}
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	bid, err := repository.FindBidByIdAndUserAccess(config.DB, id, username)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bid)
}

func PutBidStatus(c *gin.Context) {
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
	}
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	status := c.Query("status")
	if status != model.BidStatusCanceled && status != model.BidStatusPublished && status != model.BidStatusCreated {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "status must be one of these [\"Created\" \"Published\" \"Canceled\"]"})
		return
	}

	bid, err := repository.SetBidStatusById(config.DB, id, username, status)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bid)
}

func PatchBid(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
		return
	}

	var body model.Bid

	if err := c.BindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	bid, err := repository.UpdateBidById(config.DB, id, username, body)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bid)
}

func RollbackBid(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
		return
	}
	version, err := strconv.Atoi(c.Param("version"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "version must be an integer"})
		return
	}

	bid, err := repository.RollbackBidById(config.DB, id, version, username)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bid)
}

func SubmitDecision(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
		return
	}
	decision := c.Query("decision")
	if decision != model.BidStatusCanceled && decision != model.BidStatusPublished && decision != model.BidStatusCreated {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "decision must be one of these [\"Approved\" \"Rejected\"]"})
		return
	}

	bid, err := repository.SubmitDecision(config.DB, id, username, decision)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, bid)
}
