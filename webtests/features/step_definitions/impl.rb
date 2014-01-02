include Selenium::WebDriver

def time_out
  15
end

def waitfor_title(title, msg)
  wait = Wait.new(:timeout => time_out, :message => msg)
  wait.until do
    browser.title == title
  end
end

def waitfor_element(xpath, msg)
  wait = Wait.new(:timeout => time_out, :message => msg)
  wait.until do
    browser.find_element(:xpath, xpath)
  end
end

def find(xpath, msg, allow_invisible=false)
  e = nil
  wait = Wait.new(:timeout => time_out, :message => msg)
  wait.until do
    e = browser.find_element :xpath, xpath
    allow_invisible or e.displayed?
  end
  e
end

def hasText(text)
  "normalize-space(.)='#{text}'"
end

def hasNotText(text)
  "normalize-space(.)!='#{text}'"
end

def hasClass(name)
  "contains(@class, '#{name}')"
end

def click(xpath, msg)
  find(xpath, msg).click
end

def go_to_category(slug)
  click("//a[@id='link-category-#{slug}']", "Can't find category #{slug}.")
end

def go_to_product(slug)
  click("//a[@id='link-product-#{slug}']", "Can't find product #{slug}.")
end

def fill_signup(name, surname, user, passwd)
  wait = Selenium::WebDriver::Wait.new(:timeout => time_out, :message => "Can't fill sign up data.")
  wait.until do
    browser.find_element(:xpath, "//input[@id='sign-up-firstName']").send_keys name
    browser.find_element(:xpath, "//input[@id='sign-up-lastName']").send_keys surname
    browser.find_element(:xpath, "//input[@id='sign-up-email']").send_keys user
    browser.find_element(:xpath, "//input[@id='sign-up-password']").send_keys passwd
  end
end

def fill_login(user, passwd)
  wait = Selenium::WebDriver::Wait.new(:timeout => time_out, :message => "Can't fill login data.")
  wait.until do
    browser.find_element(:xpath, "//input[@id='log-in-email']").send_keys user
    browser.find_element(:xpath, "//input[@id='log-in-password']").send_keys passwd
  end
end