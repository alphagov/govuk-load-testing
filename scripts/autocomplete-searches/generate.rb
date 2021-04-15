require 'addressable'
require 'csv'

def generate_autocomplete_file(file_path)
  loaded_file = CSV.parse(File.read(file_path), headers: true)
  output_file = 'autocomplete-searches.csv'
  CSV.open(output_file, 'w' ) do |writer|
    writer << ["search_term"]
    loaded_file.each do |search|
      full_search_string = search['search_term']
      3.upto(full_search_string.length).each do |end_character_index|
        uncompleted_search_term = full_search_string[0...end_character_index]
        url = "/autocomplete_suggestions/#{uncompleted_search_term.gsub('/', '')}"
        writer << [Addressable::URI.encode(url)]
      end
    end
  end
end

file_path = ARGV[0]
generate_autocomplete_file(file_path)
