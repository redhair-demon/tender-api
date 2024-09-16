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

func queryPage(c *gin.Context) repository.Page {
	limit, err := strconv.Atoi(c.Query("limit"))
	if err != nil {
		limit = 5
	}
	offset, err := strconv.Atoi(c.Query("offset"))
	if err != nil {
		offset = 0
	}
	return repository.Page{Limit: limit, Offset: offset}
}

func GetAllTenders(c *gin.Context) {
	page := queryPage(c)
	tenders, err := repository.FindAllTenders(config.DB, page)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tenders)
}

func GetTenders(c *gin.Context) {
	page := queryPage(c)
	serviceType := c.Request.URL.Query()["service_type"]
	tenders, err := repository.FindPublishedTenders(config.DB, serviceType, page)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tenders)
}

func PostTender(c *gin.Context) {
	var body model.Tender

	if err := c.BindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	tender := model.NewTender(body.Name, body.Description, body.ServiceType, body.CreatorUsername)

	createdTender, err := repository.StoreTender(config.DB, *tender)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, createdTender)
}

func GetMyTenders(c *gin.Context) {
	page := queryPage(c)
	username := c.Query("username")
	if username == "" {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "username is required"})
		return
	}

	tenders, err := repository.FindTendersByUsername(config.DB, username, page)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tenders)
}

func GetTenderStatus(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	username := c.Query("username")
	tender, err := repository.FindTenderByIdAndUserAccess(config.DB, id, username)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tender.Status)
}

func PutTenderStatus(c *gin.Context) {
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
	status := c.Query("status")
	if status != model.TenderStatusClosed && status != model.TenderStatusPublished && status != model.TenderStatusCreated {
		c.JSON(http.StatusBadRequest, gin.H{"reason": "status must be one of these [\"Created\" \"Published\" \"Closed\"]"})
		return
	}
	tender, err := repository.SetTenderStatusById(config.DB, id, username, status)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tender)
}

func PatchTender(c *gin.Context) {
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

	var body model.Tender

	if err := c.BindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}

	tender, err := repository.UpdateTenderById(config.DB, id, username, body)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tender)
}

func RollbackTender(c *gin.Context) {
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

	tender, err := repository.RollbackTenderById(config.DB, id, version, username)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"reason": err.Error()})
		return
	}
	c.JSON(http.StatusOK, tender)
}
