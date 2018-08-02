package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
)

const (
	port = "8100"
)

func main() {
	startHttpServer()
}

func startHttpServer() {
	// setup REST endpoint (yay - this is not really REST - I know - but sufficient for this example)
	http.HandleFunc("/payment", handleHttpRequest)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func handleHttpRequest(w http.ResponseWriter, r *http.Request) {
	bodyBytes, _ := ioutil.ReadAll(r.Body)
	jsonStr := string(bodyBytes)
	fmt.Println("Retrieving payment request" + jsonStr)

	err := chargeCreditCard(jsonStr)
	if err != nil {
		w.WriteHeader(500)
	} else {
		w.WriteHeader(http.StatusOK)
	}
}

func chargeCreditCard(someDataAsJson string) error {
	_, err := doHttpCall(someDataAsJson)
	if err != nil {
		fmt.Println("Error: " + err.Error())
		return err
	} else {
		return nil
	}
}

func doHttpCall(someDataAsJson string) (resp *http.Response, err error) {
	fmt.Println("Doing http call", someDataAsJson)
	return http.Post("http://localhost:8099/charge", "application/json", strings.NewReader(someDataAsJson))
}
