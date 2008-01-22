# 
# distance.rb
 
require 'java'

class Distance
  import 'com.sun.script.jruby.test.DistConversion'
  
  def get_mi_from_km(k)
    return k/1.609
  end
  def get_km_from_mi(m)
    return m*1.609
  end
end

Distance.new
