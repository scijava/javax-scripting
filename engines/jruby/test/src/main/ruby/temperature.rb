# 
# temperature.rb
 
require 'java'

include_class 'com.sun.script.jruby.test.TempConversion'
def get_c_from_f(f)
  return (f-32.0)*5.0/9.0
end
def get_f_from_c(c)
  return c*9.0/5.0+32.0
end
