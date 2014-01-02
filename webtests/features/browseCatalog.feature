Feature: Browse catalog
  In order to buy products
  As a customer
  I want to browse the catalog and save those products of my interest

  Scenario: Add a product to the cart successfully
    Given I visit the web shop
    And I select a product
    When I add the product to the cart
    And I go to the cart
    Then I have only the chosen item
    And The total price is correctly calculated


