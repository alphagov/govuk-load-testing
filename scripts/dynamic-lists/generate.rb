require 'active_support/all'
require 'yaml'
require 'csv'
require 'securerandom'

class Generator

  def initialize(question_file, base_path, cache_bust = 0)
    @questions = question_file['questions']
    @base_path = base_path
    @cache_bust = cache_bust.to_i
    generate_possible_values
    generate_paths
    write_paths
  end

  private

  attr_reader :questions, :base_path, :cache_bust
  attr_accessor :possible_values, :chosen_values

  def chosen_values
    @chosen_values ||= []
  end

  def possible_values
    @possible_values ||= []
  end

  def generate_possible_values
    questions.each_with_index do |question, index|
      values = question['options'].flat_map { |op| op['value'] }
      case question['question_type']
      when 'single'
        possible_values.insert(index, values)
      when 'multiple'
        all_combinations = (0..values.count).flat_map{|size| values.combination(size).to_a }
        possible_values.insert(index, all_combinations)
      end
    end
  end

  def write_paths
    paths = %w[questions results email-signup]
    headers = %w[base_path hits]

    CSV.open("#{base_path.delete('\/')}_paths.csv", "wb", write_headers: true, headers: headers) do |csv|
      chosen_values.each do |vs|
        paths.each do |path|
          query_string = "#{base_path}/#{path}?#{vs.to_query('c')}"
          csv << [query_string, 1]
        end

        query_string = "#{base_path}/questions?#{vs.to_query('c')}"

        questions.length.times do |page_number|
          csv << ["#{query_string}&page=#{page_number + 1}", 1]
        end
        cache_bust.times do
          csv << ["#{query_string}&cache_bust=#{SecureRandom.uuid}", 1]
        end
      end
    end
  end

  def generate_paths
    all_permutations = []

    possible_values[0].map do |first_value|
      all_permutations << [first_value]
      possible_values[1].map do |second_value|
        question = questions[1]
        if !question['depends_on'] || first_value == question['depends_on']
          all_permutations << [second_value]
          all_permutations << [first_value, second_value]
        end
        possible_values[2].map do |third_value|
          all_permutations << [third_value]
          all_permutations << [first_value, third_value]
          all_permutations << [second_value, third_value]
          all_permutations << [first_value, second_value, third_value]
        end
      end
    end

     all_permutations.each_with_object({choice: []}) do |perm, chosen|
        chosen[:choice] = [perm].flatten
        chosen_values << chosen[:choice]
    end
  end
end


file_path = ARGV[0]
base_path = ARGV[1]
cache_bust = ARGV[2]

loaded_file = YAML.load_file(file_path)

Generator.new(loaded_file, base_path, cache_bust)
