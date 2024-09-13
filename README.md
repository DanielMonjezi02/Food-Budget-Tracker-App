
# Food Budget Tracker App

# Table of Contents

* [About Project](#about-project)
* [Video Showcase](#video-showcase)
* [Getting Started](#getting-started)
* [Known Issues](#known-issues)

## About Project

This Android mobile application, developed with Android Studio, helps users manage their spending across various food categories. The app enables users to track their grocery expenses by simply scanning products while shopping. Upon scanning, the app retrieves and displays essential details such as the product's name, price (specific to the store), and category. Users can then assign each product to a particular category, like Sweets, Drinks, or Fruit. The app provides a clear summary of expenditures, allowing users to see, for example, how much they've spent on drinks over a week. This helps users monitor their spending habits and make informed decisions about their grocery purchases to better manage their budget.

The app contains the following functions: 
* **Login/Signup System**
    - [Firebase](https://firebase.google.com/) used to implement this feature.
        - User can login and sign up.
        - User can reset their password via email.
* **Diary Page**
    - Provides a detailed breakdown of spending in each category over weekly periods. 
    - Add and delete categories.
    - Make edits to added products.
* **Product Scanning**
    - Can scan the bardoce of products they purchase while shopping. 
    - Can manually enter barcode number if scanner is not working. 
    - Can manually create a product if the product can not be found within the food database. 
* **Product Information Retrieval**
    - Request sent to food database to retrieve name and category associated with product. 
    - Request sent to food price database to retrieve product price assoicated with the store the user is within as many stores have different prices for products. 
* **Price Alert**
    - Compares the current product price with the last recorded price when the item was added to the user's diary.
    - Alerts the user to any price changes, indicating whether the price has increased or decreased based on their last purchase, helping them make more informed purchasing decisions.
* **Real Time Shopping List**
    - User is able to create a shopping list and remove an item from the shopping list when they scan a product and track it into their Diary,
    - Shopping list can be shared with any valid user within the database.
        - Owner of the shopping list is able to give read or write permissions when sharing their shopping list to other users.
        - The shopping list updates in real time for all users sharing the list. Any additions or removals are instantly reflected, ensuring that everyone viewing the shopping list sees the most up-to-date version without needing to refresh the screen.
* **Detailed Spendings**
    - User is able to specificy a specfici timeframe (E.g. 12th March - August 10th) and view a detailed overview of their spendings in each category.
    - Pie charts and bar charts are displayed to provide a clearer and more intuitive way to visualize the information, making it easier for users to analyze their spending patterns.

## Video Showcase
Click the image below to view a video showcasing the entire app and how to use each feature:
[![App Showcase](https://img.youtube.com/vi/dMfNfRmLQ7c/0.jpg)](https://youtu.be/dMfNfRmLQ7c)

## Getting Started
This section will cover how to setup the project and get it running within your system.

1. Download the repo 
2. Install [Android Studio](https://developer.android.com/studio)
3. Open the project folder within Android Studio
4. Create a device within device manager (The app was created using Pixel 6 Pro device therefore it is suggested to use that for stable performance)
5. Run the program

\
**Alternatively**, you can install the app within your Android device. Simply locate the "Food_Tracker.apk" located within the Food Tracker folder after installing the repo and open this file within your Android device. 

## Known Issues
- Clicking diary within the navigation bar will highlight "Home" as if the user is within the Home page instead of the Diary page. 
- Declining the location and camera permission during the inital usage of the app may result in the permission request not occuring again resulting in the user being unable to the scanner. 

