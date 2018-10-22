require 'csv'

outfname = ARGV.shift
infname = ARGV.shift

hit_threshold = 3

buckets = {}
puts "[pass 1] reading #{infname}"
CSV.foreach(infname) do |row|
  bucket = row[1]
  hits = row[2].to_i

  next if hits < hit_threshold

  buckets[bucket] = hits + buckets.fetch(bucket, 0)
end

top_bucket = nil
top_bucket_hits = -1
buckets.each do |bucket, hits|
  if hits > top_bucket_hits
    top_bucket = bucket
    top_bucket_hits = hits
  end
end

paths = {}
puts "[pass 2] reading #{infname}"
CSV.foreach(infname) do |row|
  path = row[0]
  bucket = row[1]
  hits = row[2].to_i

  next unless bucket == top_bucket
  next if hits < hit_threshold

  paths[path] = hits + paths.fetch(path, 0)
end

puts "writing #{outfname}"
CSV.open(outfname, 'w', force_quotes: true) do |csv|
  paths.each do |path, hits|
    csv << [path, top_bucket, hits]
  end
end
