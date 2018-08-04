# StockWatch

<b>Description</b>
- At-a-glance stock tracking app using data from Market Watch (https://www.marketwatch.com/)

<b>Installation</b>
- USB Cable
- Android device or emulator with minimum SDK 21
- Android Studio or equivalent IDE

<b>Usage</b>

<div class="row">
    <p>Upon app installation and initial launch there will be no entires in the list but after adding a few it may look like this. The Symbol and company name are on the left, the middle is the current price, and the right is the price change and the change percentage in parentheses.
    </p>
    <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/main_act.png" height="400" >
    </p>
  <p>To add more stock entries, select the plus button in the upper right corner:</p>
    <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/add_stock_dialog.png" height="400" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/add_stock_jnj.png" height="400" >
    </p>
  <p>Enter a stock symbol and the app makes an asyncronous call and downloads a JSON file that it will parse and then present you with your options:</p>
   <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/select_dialog.png" height="400" >
     <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/new_list_jnj.png" height="400" >
    </p>
  <p>After you select the symbol you want, the list will reload and alphabetize. Clicking on a list entry will open the corresponding Market Watch page in a browser.</p>
  <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/market_watch_jnj.png" height="400" >
    </p>
  <p>Back in the app, if you'd like to delete a stock entry, long clicking on it opens a delete dialog</p>
  <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/delete_dialog.png" height="400" >
    </p>
  <p>The app stores Company Names and Symbols in an SQLite database and then loads the most recent stock data upon opening the app. As such, if you try to open the app or refresh the app with no internet connection an error dialog will fire.</p>
    <p align="center" >
      <img src="https://github.com/annalbenson/StockWatch/blob/master/screenshots/no_net_dialog.png" height="400" >
    </p>
</div>






<b>Credits</b>
- Icon made using tool (https://github.com/romannurik/AndroidAssetStudio)
