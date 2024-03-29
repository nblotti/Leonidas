#/////////////////////////////////////////////////////////////////////////////////////////////////////////
#///                                                                                                   ///
#///                                 Example 2                                                         ///
#///                                                                                                   ///
#/////////////////////////////////////////////////////////////////////////////////////////////////////////
rm(list=ls())
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
  data <-content(order.result, as = "text")
  return(fromJSON(content(order.result, as = "text")))

}




accounts_url <- paste(server_url , "account/", sep = "", collapse = NULL)
account_data <- '{"entryDate":"2000-01-01","performanceCurrency":"USD"}'
raw.result <- doCallJsonRestApi('POST',accounts_url,  account_data)

accounts_url <- paste(server_url , "account/", sep = "", collapse = NULL)
account_data <- '{"entryDate":"2000-01-01","performanceCurrency":"USD"}'
raw.result <- doCallJsonRestApi('POST',accounts_url,  account_data)


order_url <- paste(server_url , "orders", sep = "", collapse = NULL)
cash_usd_order_data <- '{"accountId":"1","cIOrdID":"INITIAL_CASH" ,"side":"1","amount":"100000","type":"CASH_ENTRY","cashCurrency":"USD","transactTime":"2000-01-01"}'
order.result <- doCallJsonRestApi('POST',order_url,  cash_usd_order_data)


order_url <- paste(server_url , "orders", sep = "", collapse = NULL)
cash_usd_order_data <- '{"accountId":"2","cIOrdID":"INITIAL_CASH" ,"side":"1","amount":"100000","type":"CASH_ENTRY","cashCurrency":"USD","transactTime":"2000-01-01"}'
order.result <- doCallJsonRestApi('POST',order_url,  cash_usd_order_data)


fb_order_data <- '{"accountId":"2","cIOrdID":"SP_500" ,"side":"1","exchange":"US","type":"MARKET_ORDER","symbol":"SPY","orderQtyData":100,"transactTime":"2000-01-01"}'
order.result <- doCallJsonRestApi('POST',order_url,  fb_order_data)


fb_order_data <- '{"accountId":"1","cIOrdID":"REIT_ETF" ,"side":"1","exchange":"US","type":"MARKET_ORDER","symbol":"RWR","orderQtyData":100,"transactTime":"2001-04-24"}'
order.result <- doCallJsonRestApi('POST',order_url,  fb_order_data)

ai_order_data <- '{"accountId":"1","cIOrdID":"MATERIAL_SECTOR" ,"side":"1","exchange":"US","type":"MARKET_ORDER","symbol":"XLB","orderQtyData":100,"transactTime":"2000-01-01"}'
order.result <- doCallJsonRestApi('POST',order_url,  ai_order_data)


performance_url <- paste(server_url , "performance/1/", sep = "", collapse = NULL)

performance <- doCallJsonRestApi('GET',performance_url)

perf_totale <- performance %>% filter(type == "1")
perf_maket <- performance %>% filter(type == "2")
perf_cash <- performance %>% filter(type == "3")

perf_for_graph <- dplyr::inner_join(perf_totale, perf_maket, by = "posDate") %>% inner_join(., perf_cash, by = "posDate")
head(perf_for_graph)

df <- data.frame(perf_for_graph)


performance2_url <- paste(server_url , "performance/2/", sep = "", collapse = NULL)

performance2 <- doCallJsonRestApi('GET',performance2_url)

perf2_totale <- performance2 %>% filter(type == "1")
perf2_maket <- performance2 %>% filter(type == "2")
perf2_cash <- performance2 %>% filter(type == "3")

perf2_for_graph <- dplyr::inner_join(perf2_totale, perf2_maket, by = "posDate") %>% inner_join(., perf2_cash, by = "posDate")
head(perf2_for_graph)

df2 <- data.frame(perf2_for_graph)

perf_all_for_graph <- dplyr::inner_join(perf_totale, perf2_totale, by = "posDate") 


p <- ggplot(perf_all_for_graph, aes(as.Date(posDate), color = variable, group = 1)) +
  geom_line(aes(y =perf.x, col = "sp")) +
  geom_line(aes(y = perf.y, col = "RWR"))

p + scale_x_date(date_labels = "%b/%Y")
#rm(list=ls())
