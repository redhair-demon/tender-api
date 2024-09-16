FROM golang:1.22.5-alpine AS builder

WORKDIR /app

COPY ./src/main/go/go.mod ./src/main/go/go.sum ./
RUN go mod download

COPY ./src/main/go/ ./

RUN go build -o main.go

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/main.go .

ENV SERVER_ADDRESS=0.0.0.0:8080

EXPOSE 8080

CMD ["./main"]