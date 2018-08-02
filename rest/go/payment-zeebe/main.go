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
	startHTTPServer()
}

func startHTTPServer() {
	// setup REST endpoint (yay - this is not really REST - I know - but sufficient for this example)
	http.HandleFunc("/payment", handleHTTPRequest)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func handleHTTPRequest(w http.ResponseWriter, r *http.Request) {
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

func chargeCreditCard(someDataAsJSON string) error {
	_, err := doHTTPCall(someDataAsJSON)
	if err != nil {
		fmt.Println("Error: " + err.Error())
		return err
	}
	return nil
}

func doHTTPCall(someDataAsJSON string) (resp *http.Response, err error) {
	fmt.Println("Doing http call", someDataAsJSON)
	return http.Post("http://localhost:8099/charge", "application/json", strings.NewReader(someDataAsJSON))
}
