require 'csv'

outfname = ARGV.shift
infname = ARGV.shift

paths = {}
current_path = nil
buckets = {}
puts "reading #{infname}"
CSV.foreach(infname) do |row|
  path = row[0]
  bucket = row[1]
  hits = row[2]

  current_path = path if current_path.nil?

  unless path == current_path
    top_bucket, top_hits = buckets.max_by { |_, h| h }
    paths[current_path] = [top_bucket, top_hits]

    current_path = path
    buckets = {}
  end

  buckets[bucket] = hits.to_i + buckets.fetch(bucket, 0)
end

puts "writing #{outfname}"
CSV.open(outfname, 'w', force_quotes: true) do |csv|
  paths.each do |path, info|
    bucket = info[0]
    hits = info[1]
    csv << [path, bucket, hits]
  end
end
