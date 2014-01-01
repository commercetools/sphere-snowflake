require 'tmpdir'
require 'selenium-webdriver'
require 'net/http'
require 'net/https'
require 'uri'

require File.join(File.dirname(__FILE__), '../../cucumber-selenium/support/modules/', 'cucumberselenium.rb')

$test_options = {}
$test_options[:step_by_step] = ENV['STEP_BY_STEP'] == "true"
$test_options[:headless] = ENV['HEADLESS'] == "true"
$test_options[:record_all_videos] = ENV['RECORD_ALL_VIDEOS'] == "true"
$test_options[:fail_fast] = ENV['FAIL_FAST'] == "true"
$test_options[:verbose] = ENV['VERBOSE'] == "true"
$test_options[:run_on_saucelabs] = ENV['RUN_ON_SAUCELABS'] == "true"
$test_options[:ignore_basic_auth] = ENV['IGNORE_BASIC_AUTH'] == "true"
$test_options[:ignore_proxy] = ENV['IGNORE_PROXY'] == "true"

World(CucumberSelenium::HeadlessHelper,
CucumberSelenium::WebDriverHelper,
CucumberSelenium::Tools,
CucumberSelenium::Config,
CucumberSelenium::TestContext)

CucumberSelenium::Config.load_config
CucumberSelenium::TestContext.create_test_context

def config_sut
  if test_config['proxy'].nil?
    test_config['assume_untrusted_certificate_issuer'] = true
    sut = ENV['SYSTEM_UNDER_TEST']
    case sut
    when "production"
      # we don use the proxy in production for testing, yet
    when "vagrant"
      test_config['proxy'] = {}
      test_config['proxy']['host'] = "localhost"
      test_config['proxy']['port'] = 3128
      test_config['assume_untrusted_certificate_issuer'] = false
    when "staging"
      test_config['proxy'] = {}
      test_config['proxy']['host'] = "proxy.grid.cloud.commercetools.de"
      test_config['proxy']['port'] = 3128
    else
      Cucumber.wants_to_quit = true
      raise "Unknown SYSTEM_UNDER_TEST: #{sut}"
    end
  end
  if $test_options[:ignore_basic_auth]
    test_config['basic_auth'] = nil
  end
  if $test_options[:ignore_proxy]
    test_config['proxy'] = nil
  end
end

Before do
  if not $sut_configured
    config_sut
    $sut_configured = true
  end
end

AfterStep do
  ask "Press any key continue to next Cucumber test step" if $test_options[:step_by_step]
end

Before do |scenario|
  fail if Cucumber.wants_to_quit

  if $test_options[:run_on_saucelabs]
    $test_options[:headless] = false # ensures that After hook works as expected

    build_info = ENV['BUILD_TAG']
    build_info ||= "localdev-#{ENV['USER']}"
    sauce_opts = {
      :name => scenario.name,
      :build => build_info
    }

    caps = Selenium::WebDriver::Remote::Capabilities.internet_explorer sauce_opts
    caps.platform = 'Windows 2008'
    caps.version = '9'

    start_sauce_labs_browser(caps)
  else
    start_headless if $test_options[:headless]
    start_browser "commercetools webtest", test_config['assume_untrusted_certificate_issuer']
    start_video_capturing if $test_options[:headless]
  end

  @identifier = "#{Time.now.strftime('%Y%m%d%H%M%S')}-#{Random.new.rand(10..99)}"

end

After do |scenario|
  test_context.clear
  if $test_options[:run_on_saucelabs]
    session_id = browser.instance_variable_get("@bridge").instance_variable_get("@session_id")
    set_saucelabs_test_status session_id, (!scenario.failed?).to_s
  end

  folder = ENV['WORKSPACE']
  folder ||= Dir.tmpdir

  prefix = ENV['BUILD_NUMBER']
  prefix ||= ""

  stop_and_store_video(scenario, !$test_options[:record_all_videos], folder, prefix) if $test_options[:headless]

  stop_browser
  stop_headless if $test_options[:headless]
  Cucumber.wants_to_quit = true if scenario.failed? and $test_options[:fail_fast]

end