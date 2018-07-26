package main

import (
    "fmt"
    "html"
    "log"
    "net/http"
    "io/ioutil"
)

func main() {
    http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
        fmt.Fprintf(w, "Hello, %q", html.EscapeString(r.URL.Path))

        bodyBytes, _ := ioutil.ReadAll(r.Body)
    //jsonStr := string(bodyBytes)
    
        fmt.Println("handle credit card payment ", string(bodyBytes))
    })

    log.Fatal(http.ListenAndServe(":8099", nil))

}