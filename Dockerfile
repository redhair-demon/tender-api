FROM golang:1.22.5-alpine as builder

WORKDIR /app

COPY ./src/main/go/go.mod ./src/main/go/go.sum ./
RUN go mod download

COPY . .

RUN go build -o main .

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/main .

ENV SERVER_ADDRESS=0.0.0.0:8080

EXPOSE 8080

CMD ["./src/main/go/main"]