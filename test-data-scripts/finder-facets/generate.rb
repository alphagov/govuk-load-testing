require 'active_support/all'
require 'yaml'

def pick_number_upto(max, bias = 1)
  if max == bias || Random.rand() <= 0.6
    Random.rand(bias)
  else
    bias + Random.rand(max - bias)
  end
end

def generate_value(facet)
  case facet['type']
  when 'text'
    # text fields can take on several values, so pick a number to
    # take, biassed towards 1.
    candidates = facet['allowed_values'].map { |fv| fv['value'] }
    to_take = 1 + pick_number_upto(candidates.count)
    candidates.shuffle.take(to_take)
  when 'date'
    # dates in finders are kind of flexible, so just generate
    # something plausible-looking here and let the date-mangling logic
    # turn interpret it as it will
    year = Random.rand(100)
    month = Random.rand(12) + 1
    day = Random.rand(31) + 1
    "#{year}-#{month}-#{day}"
  else
    nil
  end
end

definition_file = ARGV.shift

definition = YAML.load_file(definition_file)

# configuration
num_paths = ENV.fetch('NUM_PATHS', '100').to_i
base_path = ENV.fetch('BASE_PATH', definition.fetch('base_path', nil))
facets = definition.fetch('facets', definition.fetch('details', {}).fetch('facets', {})).flat_map do |facet|
  # a date "facet" is actually two facets: a from and a to
  case facet['type']
  when 'date'
    ffrom = facet.merge('key' => "#{facet['key']}[from]")
    fto = facet.merge('key' => "#{facet['key']}[to]")
    [ffrom, fto]
  else
    [facet]
  end
end

# pick 'num_paths' random values of each filterable facet (potentially non-unique)
facet_values = []
num_paths.times do |i|
  facet_values << facets.each_with_object({}) do |facet, chosen|
    next unless facet['filterable']
    chosen[facet['key']] = generate_value(facet)
  end
end

# turn facet values into a list of paths
facet_values.each do |vs|
  puts "#{base_path}?#{vs.to_query}"
end
