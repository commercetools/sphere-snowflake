# encoding: utf-8


Given /^I visit the web shop$/ do
    browser.manage.window.resize_to(1024, 786)
    browser.navigate.to "http://localhost:9000/"
end

When /^I try to sign up with valid data$/ do
    click("//a[@id='link-sign-up']", "Can't find sign up page link.")
    fill_signup("Jane", "Doe", "webtests+snowflake-#{@identifier}@commercetools.de", "Test123!")
    click("//form[@id='form-sign-up']//button[@type='submit']", "Can't find sign up submit button.")
end

Then /^I am logged in$/ do
    find("//a[@id='link-log-out']", "Can't find log out link.")
end

When /^I move to a category$/ do
    go_to_category("boys-1f6b80a7-f043-4640-bb63-ed6e15527f18")
end

When /^I select a product$/ do
    go_to_category("boys-1f6b80a7-f043-4640-bb63-ed6e15527f18")
    go_to_product("boys-bonded-fleece")
    go_to_product("boys-bonded-fleece-11")
    go_to_product("boys-bonded-fleece-14")
end

When /^I go to the cart$/ do
    click("//a[@id='link-cart']", "Can't find cart link.")
end

When /^I add the product to the cart$/ do
    click("//form[@id='form-add-to-cart']//button[@type='submit']", "Can't find add to cart button.")
    waitfor_element("//form[@id='form-add-to-cart']//button[@type='submit' and #{hasNotText("Adding...")}]", "Add to cart button is always loading.")
end

Then /^I have only the chosen item$/ do
    items = find("//ul[@id='cart-content']/li[#{hasClass("item-line")}]", "Can't find chosen item.")
    return false unless items.size == 1
    find("//div[@id='item-line-product-272abd1c-467b-46c7-b8cd-be17a19261a7-11']", "Can't find chosen item name.")
end

Then /^The total price is correctly calculated$/ do
    find("//ul[@id='order-summary']/li[#{hasClass("subtotal")}]/span[#{hasText("23,00 €")}]", "Can't find correct price.")
end