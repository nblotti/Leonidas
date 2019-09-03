#/////////////////////////////////////////////////////////////////////////////////////////////////////////
#///                                                                                                   ///
#///                                 Example 2                                                         ///
#///                                                                                                   ///
#/////////////////////////////////////////////////////////////////////////////////////////////////////////
library(httr)
library(jsonlite)
library(lubridate)
library(ggplot2)
library(dplyr)

require(httr)
require(jsonlite)
require(lubridate)
require(ggplot2)
require(dplyr)

options(stringsAsFactors = FALSE)
server_url  <- "http://localhost:8080/"




doCallJsonRestApi <- function(verb, url, data) {
  
  repeat {
    switch(verb,
           'GET'= {
             order.result  <- GET(url)
           },
           POST= {
             order.result  <- POST(url, content_type_json(), body = data)
           }
    )
    
    if(order.result$status_code !=503){
      break
    }else {
      Sys.sleep(0.1)
    }
  }
  return(fromJSON(content(order.result, as = "text")))
  
}




accounts_url <- paste(server_url , "account/", sep = "", collapse = NULL)
account_data <- '{"entryDate":"2000-05-15","performanceCurrency":"CHF"}'
raw.result <- doCallJsonRestApi('POST',accounts_url,  account_data)



order_url <- paste(server_url , "orders", sep = "", collapse = NULL)
cash_usd_order_data <- '{"accountId":"1","cIOrdID":"test1" ,"side":"2","amount":"100000","type":"CASH_ENTRY","cashCurrency":"USD","transactTime":"2002-12-30"}'
order.result <- doCallJsonRestApi('POST',order_url,  cash_usd_order_data)


fb_order_data <- '{"accountId":"1","cIOrdID":"test1" ,"side":"1","exchange":"US","type":"MARKET_ORDER","symbol":"SHY","orderQtyData":166,"transactTime":"2002-12-30"}'
order.result <- doCallJsonRestApi('POST',order_url,  fb_order_data)

ai_order_data <- '{"accountId":"1","cIOrdID":"test1" ,"side":"1","exchange":"US","type":"MARKET_ORDER","symbol":"SPY","orderQtyData":1300,"transactTime":"2002-12-30"}'
order.result <- doCallJsonRestApi('POST',order_url,  ai_order_data)


performance_url <- paste(server_url , "performance/ytd/1/", sep = "", collapse = NULL)

performance <- doCallJsonRestApi('GET',performance_url)

perf_totale <- performance %>% filter(type == "1")
perf_maket <- performance %>% filter(type == "2")
perf_cash <- performance %>% filter(type == "3")

perf_for_graph <- dplyr::inner_join(perf_totale, perf_maket, by = "posDate") %>% inner_join(., perf_cash, by = "posDate")
head(perf_for_graph)

df <- data.frame(perf_for_graph)

p <- ggplot(df, aes(as.Date(posDate), color = variable, group = 1)) +
geom_line(aes(y =perf.x, col = "total")) +
geom_line(aes(y = perf.y, col = "market")) +
geom_line(aes(y = perf, col = "cash"))

p + scale_x_date(date_labels = "%b/%Y")



#rm(list=ls()) 
