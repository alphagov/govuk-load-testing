require 'csv'

outfname = ARGV.shift
infname = ARGV.shift

paths = {}
puts "reading #{infname}"
CSV.foreach(infname, headers: false, col_sep: ' ') do |request|
  time = DateTime.strptime(request[3], '[%d/%b/%Y:%H:%M:%S')
  req_bits = request[5].split(' ')

  method = req_bits[0]
  next unless method == 'GET'

  bucket = time.strftime('%Y-%m-%d %H:%M')
  path = req_bits[1]

  next if path.start_with? '/government/uploads'

  paths[path] = {} unless paths.has_key? path
  paths[path][bucket] = 1 + paths[path].fetch(bucket, 0)
end

puts "writing #{outfname}"
CSV.open(outfname, 'w', force_quotes: true) do |csv|
  paths.each do |path, buckets|
    buckets.each do |bucket, hits|
      csv << [path, bucket, hits]
    end
  end
end
