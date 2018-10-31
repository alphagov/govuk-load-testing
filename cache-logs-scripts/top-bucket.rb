require 'csv'

outfname = ARGV.shift
infname = ARGV.shift

buckets = {}
puts "[pass 1] reading #{infname}"
CSV.foreach(infname) do |row|
  bucket = row[1]
  hits = row[2].to_i

  buckets[bucket] = hits + buckets.fetch(bucket, 0)
end

top_bucket, _ = buckets.max_by { |_, h| h }

paths = {}
puts "[pass 2] reading #{infname}"
CSV.foreach(infname) do |row|
  path = row[0]
  bucket = row[1]
  hits = row[2].to_i

  next unless bucket == top_bucket

  paths[path] = hits + paths.fetch(path, 0)
end

puts "writing #{outfname}"
CSV.open(outfname, 'w', force_quotes: true) do |csv|
  paths.each do |path, hits|
    csv << [path, top_bucket, hits]
  end
end
