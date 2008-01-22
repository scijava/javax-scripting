# 
# flowers.rb

class Flowers
  @@hash = {'red' => 'ruby', 'white' => 'pearl'}
  def initialize(color, names)
    @color = color
    @names = names
  end
  
  def comment
    $, = ", "
    puts "#{@names}. Beautiful like a #{@@hash[@color]}!"
  end
  
  def others(index)
    @names.delete_at(index)
    puts "Others are #{@names}"
  end
end

$red = Flowers.new('red', ["Hibiscus", "Poinsettia", "Camellia"])
$white = Flowers.new('white',["Gardenia", "Lily", "Narusissus"])