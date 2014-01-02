Feature: Sign Up
  In order to place orders more easily and take advantage of many other benefits
  As an anonymous customer
  I want to sign up a new customer account

  Scenario: Successful sign up
    Given I visit the web shop
    When I try to sign up with valid data
    Then I am logged in