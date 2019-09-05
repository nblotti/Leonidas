<div id="leo" align="center"><h1> Leonidas </h1>
</div>
<div id="leonidas-logo" align="center">
    <br />
    <img src="https://github.com/nblotti/leonidas/blob/master/src/main/resources/leonidas.png?sanitize=true" alt="Leonidas Logo" width="300"/>
    <h3>Ὦ ξεῖν', ἀγγέλλειν Λακεδαιμονίοις ὅτι τῇδε
κείμεθα, τοῖς κείνων ῥήμασι πειθόμενοι
</h3>
</div>

<div id="badges" align="center">
        <img src="https://travis-ci.org/nblotti/leonidas.svg?branch=master" alt="Leonidas Logo"/>

</div>

<div style='margin:0 auto;width:80%;'>

</div>

- [**About**](#about)
- [**Scope**](#scope)
- [**Provider**](#provider)
- [**Getting Started**](#getting-started)
- [**Interface**](#interface)
- [**Example**](#example)
- [**Contributing**](#contributing)
- [**Feedback**](#feedback)
- [**Roadmap**](#roadmap)
- [**License**](#license)
- [**Trademark**](#trademark)

## About


“There are only two hard things in Computer Science: cache invalidation and naming things.”

— Phil Karlton

Leonidas was named after the famous spartan king, who died in the battle of Thermopylae.  He is praised for his tenacity in executing orders and holding his positionPO.
## Scope

Leonidas is part of a broader project to create an open-source robo-advisor. Using Leonidas, you will be able to create new portfolios and duplicate existing ones, to submit orders and measure their effect on your portfolio positionPOS and performancePO. Details such as stock and currency contribution will help you to refine your strategy. 

The project was born after I realized that I needed a tool to construct portfolios based on mutual funds, ETFs and stocks and help me to analyze and backtest portfolio returns, risk characteristics, standard deviation, annual returns and rolling returns. 

## Provider

Leonidas is designed to connect with a financial data provider. I use [eodhistoricaldata.com](https://eodhistoricaldata.com/) : they provide cheap daily historical stock prices for stocks, ETFs and Mutual Funds all around the world. But you can easily  use another provider by adapting three classes : QuoteService, FXQuoteService and AssetService.

## Getting Started

Download and unzip the source repository, or clone it using Git: git clone https://github.com/nblotti/leonidas.git

cd into leonidas

<blockquote>
cd leonidas

</blockquote>

Add your provider key (read section [provider](#provider)):
<blockquote>
echo spring.application.eod.api.key={replace with your key} > src/main/resources/override.properties
</blockquote>

To run the application, execute:
<blockquote>
mvn package && java -jar target/leonidas-0.0.1-SNAPSHOT.jar
</blockquote>


## Interface

Leonidas provides a set of REST API interface to let you access functions and resources. Those interfaces are designed to work with the project Themistocles (not currently public), but can also be used by other tool as [Postman](https://www.getpostman.com/) or with [R](https://www.r-project.org/). 
Finally you can also use the shell console provided. 

You will find an example for each of those interface in the [scripts](https://github.com/nblotti/leonidas/tree/master/src/main/resources/scripts) folder of resources directory.

## Example

<img src="https://github.com/nblotti/leonidas/blob/master/src/main/resources/15YwBuffet.png" alt="15Y" height="500" width="1200" style="display: block;margin-left: auto;margin-right: auto;width: 50%;"/>

This is an example taken from the R script provided with the project. This graph represents the performancePO for an initial capital of 100'000 CHF invested in 2003 following the [10/90 allocation](https://www.investopedia.com/articles/personal-finance/121815/buffetts-9010-assetPO-allocation-sound.asp) advised by Warren Buffett’s in his 2013 letter to Berkshire Hathaway investors and stayed untouched (no rebalancing) since then. 

The graph displays the TWR performancePO as well as the market and change contribution to the overall peformance.


## Contributing

Please submit issues [here](https://github.com/nblotti/leonidas/issues)


## Feedback

I will not provide support until the V1 is published. 

## Roadmap

<h4>Alpha</h4>

 - __Cash available management__
 - __Benchmark__
 - __Order sequencing__ - done
 - __Fix for__ [Sonar Security and Maintainability measure](https://sonarcloud.io/dashboard?id=nblotti_leonidas)
 
 <h4>Beta - Q4 2019</h4>
 
 - __Attribution__
 - __Standard deviation, sharpe ratio__
 - __Inflation adjusted returns__
 - __VAR__
 
 <h4>V1 - Q1 2020</h4>
 
 - __Projection & scenarios (stock and FX)__


## License

- [MIT License](LICENSE)


